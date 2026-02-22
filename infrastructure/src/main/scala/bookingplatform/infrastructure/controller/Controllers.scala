package bookingplatform.infrastructure.controller

import bookingplatform.api.dto.*
import bookingplatform.api.endpoint.Endpoints
import bookingplatform.core.domain.*
import bookingplatform.core.service.*
import bookingplatform.core.port.CalendarService
import sttp.model.StatusCode
import sttp.tapir.ztapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.*
import java.util.UUID
import java.time.Instant

type AppEnv = UserService & TrainerService & SessionService & BookingService & SubscriptionService & WaitlistService & CalendarService

private def errorMapper(error: AppError): (StatusCode, ErrorResponse) = error match
  case AppError.NotFound(entity, id)       => (StatusCode.NotFound, ErrorResponse("NOT_FOUND", s"$entity with id $id not found"))
  case AppError.ValidationError(msg)       => (StatusCode.BadRequest, ErrorResponse("VALIDATION_ERROR", msg))
  case AppError.CapacityExceeded(sid)      => (StatusCode.Conflict, ErrorResponse("CAPACITY_EXCEEDED", s"Session $sid is full"))
  case AppError.AlreadyBooked(uid, sid)    => (StatusCode.Conflict, ErrorResponse("ALREADY_BOOKED", s"User $uid already booked session $sid"))
  case AppError.InsufficientSessions(sid)  => (StatusCode.BadRequest, ErrorResponse("INSUFFICIENT_SESSIONS", s"Subscription $sid has no remaining sessions"))
  case AppError.PaymentFailed(msg)         => (StatusCode.PaymentRequired, ErrorResponse("PAYMENT_FAILED", msg))
  case AppError.Unauthorized(msg)          => (StatusCode.Unauthorized, ErrorResponse("UNAUTHORIZED", msg))
  case AppError.InternalError(msg)         => (StatusCode.InternalServerError, ErrorResponse("INTERNAL_ERROR", msg))

private def parseUUID(s: String): IO[AppError, UUID] =
  ZIO.attempt(UUID.fromString(s)).mapError(_ => AppError.ValidationError(s"Invalid UUID: $s"))

// ─── User Controller ────────────────────
object UserController:
  val create: ZServerEndpoint[AppEnv, Any] =
    Endpoints.createUser.zServerLogic { req =>
      UserService.createUser(req.name, req.email, req.phone)
        .mapBoth(errorMapper, u => UserResponse(u.id.toString, u.name, u.email, u.phone, u.createdAt.toString))
    }

  val get: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getUser.zServerLogic { id =>
      (for
        uuid <- parseUUID(id)
        user <- UserService.getUser(uuid)
      yield UserResponse(user.id.toString, user.name, user.email, user.phone, user.createdAt.toString))
        .mapError(errorMapper)
    }

  val getAll: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getAllUsers.zServerLogic { _ =>
      UserService.getAllUsers()
        .mapBoth(errorMapper, _.map(u => UserResponse(u.id.toString, u.name, u.email, u.phone, u.createdAt.toString)))
    }

  val update: ZServerEndpoint[AppEnv, Any] =
    Endpoints.updateUser.zServerLogic { case (id, req) =>
      (for
        uuid    <- parseUUID(id)
        updated <- UserService.updateUser(uuid, req.name, req.email, req.phone)
      yield UserResponse(updated.id.toString, updated.name, updated.email, updated.phone, updated.createdAt.toString))
        .mapError(errorMapper)
    }

  val all = List(create, get, getAll, update)

// ─── Trainer Controller ────────────────────
object TrainerController:
  private def parseSessionTypes(types: List[String]): IO[AppError, List[SessionType]] =
    ZIO.foreach(types) { t =>
      ZIO.attempt(SessionType.valueOf(t)).mapError(_ => AppError.ValidationError(s"Invalid session type: $t"))
    }

  val create: ZServerEndpoint[AppEnv, Any] =
    Endpoints.createTrainer.zServerLogic { req =>
      (for
        specs   <- parseSessionTypes(req.specializations)
        trainer <- TrainerService.createTrainer(req.name, req.email, specs, req.bio)
      yield TrainerResponse(trainer.id.toString, trainer.name, trainer.email, trainer.specializations.map(_.toString), trainer.bio))
        .mapError(errorMapper)
    }

  val get: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getTrainer.zServerLogic { id =>
      (for
        uuid    <- parseUUID(id)
        trainer <- TrainerService.getTrainer(uuid)
      yield TrainerResponse(trainer.id.toString, trainer.name, trainer.email, trainer.specializations.map(_.toString), trainer.bio))
        .mapError(errorMapper)
    }

  val getAll: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getAllTrainers.zServerLogic { _ =>
      TrainerService.getAllTrainers()
        .mapBoth(errorMapper, _.map(t => TrainerResponse(t.id.toString, t.name, t.email, t.specializations.map(_.toString), t.bio)))
    }

  val all = List(create, get, getAll)

// ─── Session Controller ────────────────────
object SessionController:
  private def toSessionResponse(s: Session) =
    SessionResponse(s.id.toString, s.name, s.description, s.sessionType.toString, s.trainerId.toString,
      s.capacity, s.durationMinutes, s.price.toDouble, s.stationCount)

  private def toScheduledResponse(s: ScheduledSession) =
    ScheduledSessionResponse(s.id.toString, s.sessionId.toString, s.startTime.toString, s.endTime.toString,
      s.currentBookings, s.status.toString)

  val create: ZServerEndpoint[AppEnv, Any] =
    Endpoints.createSession.zServerLogic { req =>
      (for
        st      <- ZIO.attempt(SessionType.valueOf(req.sessionType)).mapError(_ => AppError.ValidationError(s"Invalid session type: ${req.sessionType}"))
        tid     <- parseUUID(req.trainerId)
        session <- SessionService.createSession(req.name, req.description, st, tid, req.capacity, req.durationMinutes, BigDecimal(req.price), req.stationCount)
      yield toSessionResponse(session)).mapError(errorMapper)
    }

  val get: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getSession.zServerLogic { id =>
      (for
        uuid    <- parseUUID(id)
        session <- SessionService.getSession(uuid)
      yield toSessionResponse(session)).mapError(errorMapper)
    }

  val getAll: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getAllSessions.zServerLogic { _ =>
      SessionService.getAllSessions()
        .mapBoth(errorMapper, _.map(toSessionResponse))
    }

  val schedule: ZServerEndpoint[AppEnv, Any] =
    Endpoints.scheduleSession.zServerLogic { req =>
      (for
        sid       <- parseUUID(req.sessionId)
        startTime <- ZIO.attempt(Instant.parse(req.startTime)).mapError(_ => AppError.ValidationError(s"Invalid date format: ${req.startTime}"))
        scheduled <- SessionService.scheduleSession(sid, startTime)
      yield toScheduledResponse(scheduled)).mapError(errorMapper)
    }

  val getScheduled: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getScheduledSession.zServerLogic { id =>
      (for
        uuid      <- parseUUID(id)
        scheduled <- SessionService.getScheduledSession(uuid)
      yield toScheduledResponse(scheduled)).mapError(errorMapper)
    }

  val getUpcoming: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getUpcomingScheduledSessions.zServerLogic { _ =>
      SessionService.getUpcomingScheduledSessions()
        .mapBoth(errorMapper, _.map(toScheduledResponse))
    }

  val all = List(create, get, getAll, schedule, getScheduled, getUpcoming)

// ─── Booking Controller ────────────────────
object BookingController:
  private def toBookingResponse(b: Booking) =
    BookingResponse(b.id.toString, b.userId.toString, b.scheduledSessionId.toString,
      b.status.toString, b.subscriptionId.map(_.toString), b.createdAt.toString)

  private def toCancellationResponse(c: Cancellation) =
    CancellationResponse(c.id.toString, c.bookingId.toString, c.userId.toString,
      c.reason, c.lateFee.toDouble, c.cancelledAt.toString)

  val create: ZServerEndpoint[AppEnv, Any] =
    Endpoints.createBooking.zServerLogic { req =>
      (for
        uid  <- parseUUID(req.userId)
        ssid <- parseUUID(req.scheduledSessionId)
        sid  <- ZIO.foreach(req.subscriptionId)(parseUUID)
        booking <- BookingService.createBooking(uid, ssid, sid)
      yield toBookingResponse(booking)).mapError(errorMapper)
    }

  val cancel: ZServerEndpoint[AppEnv, Any] =
    Endpoints.cancelBooking.zServerLogic { case (id, req) =>
      (for
        uuid <- parseUUID(id)
        cancellation <- BookingService.cancelBooking(uuid, req.reason)
      yield toCancellationResponse(cancellation)).mapError(errorMapper)
    }

  val get: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getBooking.zServerLogic { id =>
      (for
        uuid    <- parseUUID(id)
        booking <- BookingService.getBooking(uuid)
      yield toBookingResponse(booking)).mapError(errorMapper)
    }

  val getUserBookings: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getUserBookings.zServerLogic { userId =>
      (for
        uid      <- parseUUID(userId)
        bookings <- BookingService.getUserBookings(uid)
      yield bookings.map(toBookingResponse)).mapError(errorMapper)
    }

  val all = List(create, cancel, get, getUserBookings)

// ─── Subscription Controller ────────────────────
object SubscriptionController:
  private def toPlanResponse(p: SubscriptionPlan) =
    SubscriptionPlanResponse(p.id.toString, p.name, p.planType.toString, p.price.toDouble,
      p.sessionCount, p.validityDays, p.description)

  private def toSubResponse(s: Subscription) =
    SubscriptionResponse(s.id.toString, s.userId.toString, s.planId.toString, s.status.toString,
      s.startDate.toString, s.endDate.map(_.toString), s.remainingSessions, s.createdAt.toString)

  val createPlan: ZServerEndpoint[AppEnv, Any] =
    Endpoints.createSubscriptionPlan.zServerLogic { req =>
      (for
        pt   <- ZIO.attempt(SubscriptionPlanType.valueOf(req.planType)).mapError(_ => AppError.ValidationError(s"Invalid plan type: ${req.planType}"))
        plan <- SubscriptionService.createPlan(req.name, pt, BigDecimal(req.price), req.sessionCount, req.validityDays, req.description)
      yield toPlanResponse(plan)).mapError(errorMapper)
    }

  val getPlans: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getSubscriptionPlans.zServerLogic { _ =>
      SubscriptionService.getPlans()
        .mapBoth(errorMapper, _.map(toPlanResponse))
    }

  val subscribe: ZServerEndpoint[AppEnv, Any] =
    Endpoints.createSubscription.zServerLogic { req =>
      (for
        uid <- parseUUID(req.userId)
        pid <- parseUUID(req.planId)
        sub <- SubscriptionService.subscribe(uid, pid)
      yield toSubResponse(sub)).mapError(errorMapper)
    }

  val cancel: ZServerEndpoint[AppEnv, Any] =
    Endpoints.cancelSubscription.zServerLogic { id =>
      (for
        uuid <- parseUUID(id)
        sub  <- SubscriptionService.cancelSubscription(uuid)
      yield toSubResponse(sub)).mapError(errorMapper)
    }

  val getUserSubs: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getUserSubscriptions.zServerLogic { userId =>
      (for
        uid  <- parseUUID(userId)
        subs <- SubscriptionService.getUserSubscriptions(uid)
      yield subs.map(toSubResponse)).mapError(errorMapper)
    }

  val getSub: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getSubscription.zServerLogic { id =>
      (for
        uuid <- parseUUID(id)
        sub  <- SubscriptionService.getSubscription(uuid)
      yield toSubResponse(sub)).mapError(errorMapper)
    }

  val all = List(createPlan, getPlans, subscribe, cancel, getUserSubs, getSub)

// ─── Waitlist Controller ────────────────────
object WaitlistController:
  private def toWaitlistResponse(w: WaitlistEntry) =
    WaitlistEntryResponse(w.id.toString, w.userId.toString, w.scheduledSessionId.toString, w.position, w.createdAt.toString)

  val join: ZServerEndpoint[AppEnv, Any] =
    Endpoints.joinWaitlist.zServerLogic { req =>
      (for
        uid  <- parseUUID(req.userId)
        ssid <- parseUUID(req.scheduledSessionId)
        entry <- WaitlistService.joinWaitlist(uid, ssid)
      yield toWaitlistResponse(entry)).mapError(errorMapper)
    }

  val leave: ZServerEndpoint[AppEnv, Any] =
    Endpoints.leaveWaitlist.zServerLogic { case (userId, scheduledSessionId) =>
      (for
        uid  <- parseUUID(userId)
        ssid <- parseUUID(scheduledSessionId)
        _    <- WaitlistService.leaveWaitlist(uid, ssid)
      yield ()).mapError(errorMapper)
    }

  val get: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getWaitlist.zServerLogic { scheduledSessionId =>
      (for
        ssid    <- parseUUID(scheduledSessionId)
        entries <- WaitlistService.getWaitlist(ssid)
      yield entries.map(toWaitlistResponse)).mapError(errorMapper)
    }

  val all = List(join, leave, get)

// ─── Calendar Controller ────────────────────
object CalendarController:
  val getUserCalendar: ZServerEndpoint[AppEnv, Any] =
    Endpoints.getUserCalendar.zServerLogic { userId =>
      (for
        uid      <- parseUUID(userId)
        bookings <- BookingService.getUserBookings(uid)
        confirmed = bookings.filter(_.status == BookingStatus.Confirmed)
        events   <- ZIO.foreach(confirmed) { booking =>
          for
            scheduled <- SessionService.getScheduledSession(booking.scheduledSessionId)
            session   <- SessionService.getSession(scheduled.sessionId)
          yield (session, scheduled, booking)
        }
        ical <- ZIO.serviceWithZIO[CalendarService](_.generateICalFeed(events))
                  .mapError(e => AppError.InternalError(e.getMessage))
      yield ical).mapError(errorMapper)
    }

  val all = List(getUserCalendar)

// ─── Routes ────────────────────
object Routes:
  val serverEndpoints: List[ZServerEndpoint[AppEnv, Any]] =
    BookingController.all ++ SubscriptionController.all ++ CalendarController.all ++
    WaitlistController.all ++ UserController.all ++ TrainerController.all ++
    SessionController.all

  val swaggerEndpoints =
    SwaggerInterpreter().fromEndpoints[[A] =>> RIO[AppEnv, A]](Endpoints.all, "Booking Platform API", "1.0.0")

  val allEndpoints: List[ZServerEndpoint[AppEnv, Any]] = serverEndpoints ++ swaggerEndpoints

  def httpApp = ZioHttpInterpreter().toHttp(allEndpoints)
