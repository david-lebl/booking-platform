package bookingplatform.core.service

import bookingplatform.core.domain.*
import bookingplatform.core.port.*
import zio.*
import java.time.{Instant, LocalDate}
import java.util.UUID

// Helper for error conversion
private def liftTask[A](task: Task[A]): IO[AppError, A] =
  task.mapError(e => AppError.InternalError(e.getMessage))

// ─── User Service ──────────────────────────────────────
trait UserService:
  def createUser(name: String, email: String, phone: Option[String]): IO[AppError, User]
  def getUser(id: UserId): IO[AppError, User]
  def getUserByEmail(email: String): IO[AppError, User]
  def getAllUsers(): IO[AppError, List[User]]
  def updateUser(id: UserId, name: String, email: String, phone: Option[String]): IO[AppError, User]

object UserService:
  def createUser(name: String, email: String, phone: Option[String]): ZIO[UserService, AppError, User] =
    ZIO.serviceWithZIO[UserService](_.createUser(name, email, phone))
  def getUser(id: UserId): ZIO[UserService, AppError, User] =
    ZIO.serviceWithZIO[UserService](_.getUser(id))
  def getUserByEmail(email: String): ZIO[UserService, AppError, User] =
    ZIO.serviceWithZIO[UserService](_.getUserByEmail(email))
  def getAllUsers(): ZIO[UserService, AppError, List[User]] =
    ZIO.serviceWithZIO[UserService](_.getAllUsers())
  def updateUser(id: UserId, name: String, email: String, phone: Option[String]): ZIO[UserService, AppError, User] =
    ZIO.serviceWithZIO[UserService](_.updateUser(id, name, email, phone))

case class UserServiceLive(userRepo: UserRepository) extends UserService:
  def createUser(name: String, email: String, phone: Option[String]): IO[AppError, User] =
    for
      existing <- liftTask(userRepo.findByEmail(email))
      _        <- ZIO.when(existing.isDefined)(ZIO.fail(AppError.ValidationError(s"Email $email already in use")))
      now      <- Clock.instant
      user      = User(UUID.randomUUID(), name, email, phone, now)
      created  <- liftTask(userRepo.create(user))
    yield created

  def getUser(id: UserId): IO[AppError, User] =
    liftTask(userRepo.findById(id))
      .flatMap(_.fold(ZIO.fail(AppError.NotFound("User", id.toString)))(ZIO.succeed(_)))

  def getUserByEmail(email: String): IO[AppError, User] =
    liftTask(userRepo.findByEmail(email))
      .flatMap(_.fold(ZIO.fail(AppError.NotFound("User", email)))(ZIO.succeed(_)))

  def getAllUsers(): IO[AppError, List[User]] = liftTask(userRepo.findAll())

  def updateUser(id: UserId, name: String, email: String, phone: Option[String]): IO[AppError, User] =
    for
      existing <- getUser(id)
      updated   = existing.copy(name = name, email = email, phone = phone)
      result   <- liftTask(userRepo.update(updated))
    yield result

object UserServiceLive:
  val layer: URLayer[UserRepository, UserService] = ZLayer {
    ZIO.service[UserRepository].map(UserServiceLive(_))
  }

// ─── Trainer Service ──────────────────────────────────────
trait TrainerService:
  def createTrainer(name: String, email: String, specializations: List[SessionType], bio: Option[String]): IO[AppError, Trainer]
  def getTrainer(id: TrainerId): IO[AppError, Trainer]
  def getAllTrainers(): IO[AppError, List[Trainer]]

object TrainerService:
  def createTrainer(name: String, email: String, specializations: List[SessionType], bio: Option[String]): ZIO[TrainerService, AppError, Trainer] =
    ZIO.serviceWithZIO[TrainerService](_.createTrainer(name, email, specializations, bio))
  def getTrainer(id: TrainerId): ZIO[TrainerService, AppError, Trainer] =
    ZIO.serviceWithZIO[TrainerService](_.getTrainer(id))
  def getAllTrainers(): ZIO[TrainerService, AppError, List[Trainer]] =
    ZIO.serviceWithZIO[TrainerService](_.getAllTrainers())

case class TrainerServiceLive(trainerRepo: TrainerRepository) extends TrainerService:
  def createTrainer(name: String, email: String, specializations: List[SessionType], bio: Option[String]): IO[AppError, Trainer] =
    val trainer = Trainer(UUID.randomUUID(), name, email, specializations, bio)
    liftTask(trainerRepo.create(trainer))

  def getTrainer(id: TrainerId): IO[AppError, Trainer] =
    liftTask(trainerRepo.findById(id))
      .flatMap(_.fold(ZIO.fail(AppError.NotFound("Trainer", id.toString)))(ZIO.succeed(_)))

  def getAllTrainers(): IO[AppError, List[Trainer]] = liftTask(trainerRepo.findAll())

object TrainerServiceLive:
  val layer: URLayer[TrainerRepository, TrainerService] = ZLayer {
    ZIO.service[TrainerRepository].map(TrainerServiceLive(_))
  }

// ─── Session Service ──────────────────────────────────────
trait SessionService:
  def createSession(name: String, description: Option[String], sessionType: SessionType, trainerId: TrainerId,
                    capacity: Int, durationMinutes: Int, price: BigDecimal, stationCount: Option[Int]): IO[AppError, Session]
  def getSession(id: SessionId): IO[AppError, Session]
  def getAllSessions(): IO[AppError, List[Session]]
  def scheduleSession(sessionId: SessionId, startTime: Instant): IO[AppError, ScheduledSession]
  def getScheduledSession(id: ScheduledSessionId): IO[AppError, ScheduledSession]
  def getUpcomingScheduledSessions(): IO[AppError, List[ScheduledSession]]

object SessionService:
  def createSession(name: String, description: Option[String], sessionType: SessionType, trainerId: TrainerId,
                    capacity: Int, durationMinutes: Int, price: BigDecimal, stationCount: Option[Int]): ZIO[SessionService, AppError, Session] =
    ZIO.serviceWithZIO[SessionService](_.createSession(name, description, sessionType, trainerId, capacity, durationMinutes, price, stationCount))
  def getSession(id: SessionId): ZIO[SessionService, AppError, Session] =
    ZIO.serviceWithZIO[SessionService](_.getSession(id))
  def getAllSessions(): ZIO[SessionService, AppError, List[Session]] =
    ZIO.serviceWithZIO[SessionService](_.getAllSessions())
  def scheduleSession(sessionId: SessionId, startTime: Instant): ZIO[SessionService, AppError, ScheduledSession] =
    ZIO.serviceWithZIO[SessionService](_.scheduleSession(sessionId, startTime))
  def getScheduledSession(id: ScheduledSessionId): ZIO[SessionService, AppError, ScheduledSession] =
    ZIO.serviceWithZIO[SessionService](_.getScheduledSession(id))
  def getUpcomingScheduledSessions(): ZIO[SessionService, AppError, List[ScheduledSession]] =
    ZIO.serviceWithZIO[SessionService](_.getUpcomingScheduledSessions())

case class SessionServiceLive(
  sessionRepo: SessionRepository,
  scheduledSessionRepo: ScheduledSessionRepository,
  trainerRepo: TrainerRepository
) extends SessionService:
  def createSession(name: String, description: Option[String], sessionType: SessionType, trainerId: TrainerId,
                    capacity: Int, durationMinutes: Int, price: BigDecimal, stationCount: Option[Int]): IO[AppError, Session] =
    for
      _ <- liftTask(trainerRepo.findById(trainerId))
             .flatMap(_.fold(ZIO.fail(AppError.NotFound("Trainer", trainerId.toString)))(ZIO.succeed(_)))
      _ <- ZIO.when(capacity <= 0)(ZIO.fail(AppError.ValidationError("Capacity must be positive")))
      _ <- ZIO.when(durationMinutes <= 0)(ZIO.fail(AppError.ValidationError("Duration must be positive")))
      _ <- ZIO.when(price < 0)(ZIO.fail(AppError.ValidationError("Price cannot be negative")))
      session = Session(UUID.randomUUID(), name, description, sessionType, trainerId, capacity, durationMinutes, price, stationCount)
      created <- liftTask(sessionRepo.create(session))
    yield created

  def getSession(id: SessionId): IO[AppError, Session] =
    liftTask(sessionRepo.findById(id))
      .flatMap(_.fold(ZIO.fail(AppError.NotFound("Session", id.toString)))(ZIO.succeed(_)))

  def getAllSessions(): IO[AppError, List[Session]] = liftTask(sessionRepo.findAll())

  def scheduleSession(sessionId: SessionId, startTime: Instant): IO[AppError, ScheduledSession] =
    for
      session <- getSession(sessionId)
      endTime  = startTime.plusSeconds(session.durationMinutes.toLong * 60)
      scheduled = ScheduledSession(UUID.randomUUID(), sessionId, startTime, endTime, 0, ScheduledSessionStatus.Scheduled)
      created <- liftTask(scheduledSessionRepo.create(scheduled))
    yield created

  def getScheduledSession(id: ScheduledSessionId): IO[AppError, ScheduledSession] =
    liftTask(scheduledSessionRepo.findById(id))
      .flatMap(_.fold(ZIO.fail(AppError.NotFound("ScheduledSession", id.toString)))(ZIO.succeed(_)))

  def getUpcomingScheduledSessions(): IO[AppError, List[ScheduledSession]] =
    liftTask(scheduledSessionRepo.findUpcoming())

object SessionServiceLive:
  val layer: URLayer[SessionRepository & ScheduledSessionRepository & TrainerRepository, SessionService] = ZLayer {
    for
      sessionRepo   <- ZIO.service[SessionRepository]
      scheduledRepo <- ZIO.service[ScheduledSessionRepository]
      trainerRepo   <- ZIO.service[TrainerRepository]
    yield SessionServiceLive(sessionRepo, scheduledRepo, trainerRepo)
  }

// ─── Booking Service ──────────────────────────────────────
trait BookingService:
  def createBooking(userId: UserId, scheduledSessionId: ScheduledSessionId, subscriptionId: Option[SubscriptionId]): IO[AppError, Booking]
  def cancelBooking(bookingId: BookingId, reason: Option[String]): IO[AppError, Cancellation]
  def getBooking(id: BookingId): IO[AppError, Booking]
  def getUserBookings(userId: UserId): IO[AppError, List[Booking]]

object BookingService:
  def createBooking(userId: UserId, scheduledSessionId: ScheduledSessionId, subscriptionId: Option[SubscriptionId]): ZIO[BookingService, AppError, Booking] =
    ZIO.serviceWithZIO[BookingService](_.createBooking(userId, scheduledSessionId, subscriptionId))
  def cancelBooking(bookingId: BookingId, reason: Option[String]): ZIO[BookingService, AppError, Cancellation] =
    ZIO.serviceWithZIO[BookingService](_.cancelBooking(bookingId, reason))
  def getBooking(id: BookingId): ZIO[BookingService, AppError, Booking] =
    ZIO.serviceWithZIO[BookingService](_.getBooking(id))
  def getUserBookings(userId: UserId): ZIO[BookingService, AppError, List[Booking]] =
    ZIO.serviceWithZIO[BookingService](_.getUserBookings(userId))

case class BookingServiceLive(
  bookingRepo: BookingRepository,
  scheduledSessionRepo: ScheduledSessionRepository,
  sessionRepo: SessionRepository,
  subscriptionRepo: SubscriptionRepository,
  cancellationRepo: CancellationRepository,
  paymentGateway: PaymentGateway,
  notificationService: NotificationService
) extends BookingService:
  
  def createBooking(userId: UserId, scheduledSessionId: ScheduledSessionId, subscriptionId: Option[SubscriptionId]): IO[AppError, Booking] =
    for
      scheduled <- liftTask(scheduledSessionRepo.findById(scheduledSessionId))
                     .flatMap(_.fold(ZIO.fail(AppError.NotFound("ScheduledSession", scheduledSessionId.toString)))(ZIO.succeed(_)))
      _         <- ZIO.when(scheduled.status != ScheduledSessionStatus.Scheduled)(
                     ZIO.fail(AppError.ValidationError("Session is not available for booking")))
      session   <- liftTask(sessionRepo.findById(scheduled.sessionId))
                     .flatMap(_.fold(ZIO.fail(AppError.NotFound("Session", scheduled.sessionId.toString)))(ZIO.succeed(_)))
      existing  <- liftTask(bookingRepo.findByUserAndScheduledSession(userId, scheduledSessionId))
      _         <- ZIO.when(existing.exists(b => b.status == BookingStatus.Confirmed || b.status == BookingStatus.WaitListed))(
                     ZIO.fail(AppError.AlreadyBooked(userId.toString, scheduledSessionId.toString)))
      _         <- subscriptionId match
                     case Some(subId) =>
                       liftTask(subscriptionRepo.findById(subId)).flatMap {
                         case Some(sub) if sub.status == SubscriptionStatus.Active =>
                           sub.remainingSessions match
                             case Some(remaining) if remaining <= 0 =>
                               ZIO.fail(AppError.InsufficientSessions(subId.toString))
                             case Some(remaining) =>
                               liftTask(subscriptionRepo.update(sub.copy(remainingSessions = Some(remaining - 1)))).unit
                             case None => ZIO.unit // unlimited (monthly)
                         case Some(_) => ZIO.fail(AppError.ValidationError("Subscription is not active"))
                         case None    => ZIO.fail(AppError.NotFound("Subscription", subId.toString))
                       }
                     case None =>
                       liftTask(paymentGateway.charge(userId, session.price, s"Booking for ${session.name}")).unit
      isSessionFull = scheduled.currentBookings >= session.capacity
      status     = if isSessionFull then BookingStatus.WaitListed else BookingStatus.Confirmed
      now       <- Clock.instant
      booking    = Booking(UUID.randomUUID(), userId, scheduledSessionId, status, subscriptionId, now)
      created   <- liftTask(bookingRepo.create(booking))
      _         <- ZIO.when(!isSessionFull)(
                     liftTask(scheduledSessionRepo.update(scheduled.copy(currentBookings = scheduled.currentBookings + 1))))
    yield created

  def cancelBooking(bookingId: BookingId, reason: Option[String]): IO[AppError, Cancellation] =
    for
      booking   <- liftTask(bookingRepo.findById(bookingId))
                     .flatMap(_.fold(ZIO.fail(AppError.NotFound("Booking", bookingId.toString)))(ZIO.succeed(_)))
      _         <- ZIO.when(booking.status == BookingStatus.Cancelled)(
                     ZIO.fail(AppError.ValidationError("Booking is already cancelled")))
      scheduled <- liftTask(scheduledSessionRepo.findById(booking.scheduledSessionId))
                     .flatMap(_.fold(ZIO.fail(AppError.NotFound("ScheduledSession", booking.scheduledSessionId.toString)))(ZIO.succeed(_)))
      session   <- liftTask(sessionRepo.findById(scheduled.sessionId))
                     .flatMap(_.fold(ZIO.fail(AppError.NotFound("Session", scheduled.sessionId.toString)))(ZIO.succeed(_)))
      now       <- Clock.instant
      hoursUntilSession = java.time.Duration.between(now, scheduled.startTime).toHours
      lateFee    = if hoursUntilSession > 24 then BigDecimal(0)
                   else if hoursUntilSession > 2 then session.price * BigDecimal(0.5)
                   else session.price
      _         <- liftTask(bookingRepo.update(booking.copy(status = BookingStatus.Cancelled)))
      _         <- ZIO.when(booking.status == BookingStatus.Confirmed)(
                     liftTask(scheduledSessionRepo.update(scheduled.copy(currentBookings = (scheduled.currentBookings - 1).max(0)))))
      _         <- ZIO.when(lateFee > 0)(
                     liftTask(paymentGateway.charge(booking.userId, lateFee, s"Late cancellation fee")).unit)
      cancellation = Cancellation(UUID.randomUUID(), bookingId, booking.userId, reason, lateFee, now)
      created   <- liftTask(cancellationRepo.create(cancellation))
    yield created

  def getBooking(id: BookingId): IO[AppError, Booking] =
    liftTask(bookingRepo.findById(id))
      .flatMap(_.fold(ZIO.fail(AppError.NotFound("Booking", id.toString)))(ZIO.succeed(_)))

  def getUserBookings(userId: UserId): IO[AppError, List[Booking]] =
    liftTask(bookingRepo.findByUser(userId))

object BookingServiceLive:
  val layer: URLayer[
    BookingRepository & ScheduledSessionRepository & SessionRepository & SubscriptionRepository &
    CancellationRepository & PaymentGateway & NotificationService,
    BookingService
  ] = ZLayer {
    for
      bookingRepo      <- ZIO.service[BookingRepository]
      scheduledRepo    <- ZIO.service[ScheduledSessionRepository]
      sessionRepo      <- ZIO.service[SessionRepository]
      subscriptionRepo <- ZIO.service[SubscriptionRepository]
      cancellationRepo <- ZIO.service[CancellationRepository]
      paymentGateway   <- ZIO.service[PaymentGateway]
      notificationSvc  <- ZIO.service[NotificationService]
    yield BookingServiceLive(bookingRepo, scheduledRepo, sessionRepo, subscriptionRepo, cancellationRepo, paymentGateway, notificationSvc)
  }

// ─── Subscription Service ──────────────────────────────────────
trait SubscriptionService:
  def createPlan(name: String, planType: SubscriptionPlanType, price: BigDecimal,
                 sessionCount: Option[Int], validityDays: Option[Int], description: Option[String]): IO[AppError, SubscriptionPlan]
  def getPlans(): IO[AppError, List[SubscriptionPlan]]
  def getPlan(id: SubscriptionPlanId): IO[AppError, SubscriptionPlan]
  def subscribe(userId: UserId, planId: SubscriptionPlanId): IO[AppError, Subscription]
  def cancelSubscription(subscriptionId: SubscriptionId): IO[AppError, Subscription]
  def getUserSubscriptions(userId: UserId): IO[AppError, List[Subscription]]
  def getSubscription(id: SubscriptionId): IO[AppError, Subscription]

object SubscriptionService:
  def createPlan(name: String, planType: SubscriptionPlanType, price: BigDecimal,
                 sessionCount: Option[Int], validityDays: Option[Int], description: Option[String]): ZIO[SubscriptionService, AppError, SubscriptionPlan] =
    ZIO.serviceWithZIO[SubscriptionService](_.createPlan(name, planType, price, sessionCount, validityDays, description))
  def getPlans(): ZIO[SubscriptionService, AppError, List[SubscriptionPlan]] =
    ZIO.serviceWithZIO[SubscriptionService](_.getPlans())
  def getPlan(id: SubscriptionPlanId): ZIO[SubscriptionService, AppError, SubscriptionPlan] =
    ZIO.serviceWithZIO[SubscriptionService](_.getPlan(id))
  def subscribe(userId: UserId, planId: SubscriptionPlanId): ZIO[SubscriptionService, AppError, Subscription] =
    ZIO.serviceWithZIO[SubscriptionService](_.subscribe(userId, planId))
  def cancelSubscription(subscriptionId: SubscriptionId): ZIO[SubscriptionService, AppError, Subscription] =
    ZIO.serviceWithZIO[SubscriptionService](_.cancelSubscription(subscriptionId))
  def getUserSubscriptions(userId: UserId): ZIO[SubscriptionService, AppError, List[Subscription]] =
    ZIO.serviceWithZIO[SubscriptionService](_.getUserSubscriptions(userId))
  def getSubscription(id: SubscriptionId): ZIO[SubscriptionService, AppError, Subscription] =
    ZIO.serviceWithZIO[SubscriptionService](_.getSubscription(id))

case class SubscriptionServiceLive(
  planRepo: SubscriptionPlanRepository,
  subscriptionRepo: SubscriptionRepository,
  paymentGateway: PaymentGateway
) extends SubscriptionService:

  def createPlan(name: String, planType: SubscriptionPlanType, price: BigDecimal,
                 sessionCount: Option[Int], validityDays: Option[Int], description: Option[String]): IO[AppError, SubscriptionPlan] =
    val plan = SubscriptionPlan(UUID.randomUUID(), name, planType, price, sessionCount, validityDays, description)
    liftTask(planRepo.create(plan))

  def getPlans(): IO[AppError, List[SubscriptionPlan]] = liftTask(planRepo.findAll())

  def getPlan(id: SubscriptionPlanId): IO[AppError, SubscriptionPlan] =
    liftTask(planRepo.findById(id))
      .flatMap(_.fold(ZIO.fail(AppError.NotFound("SubscriptionPlan", id.toString)))(ZIO.succeed(_)))

  def subscribe(userId: UserId, planId: SubscriptionPlanId): IO[AppError, Subscription] =
    for
      plan <- getPlan(planId)
      _    <- liftTask(paymentGateway.charge(userId, plan.price, s"Subscription: ${plan.name}"))
      now  <- Clock.instant
      today = now.atZone(java.time.ZoneId.systemDefault()).toLocalDate
      endDate = plan.planType match
        case SubscriptionPlanType.Monthly => Some(today.plusMonths(1))
        case SubscriptionPlanType.Package => plan.validityDays.map(d => today.plusDays(d.toLong))
        case SubscriptionPlanType.OneTime => None
      remaining = plan.planType match
        case SubscriptionPlanType.Package => plan.sessionCount
        case SubscriptionPlanType.OneTime => Some(1)
        case SubscriptionPlanType.Monthly => None
      subscription = Subscription(UUID.randomUUID(), userId, planId, SubscriptionStatus.Active, today, endDate, remaining, now)
      created <- liftTask(subscriptionRepo.create(subscription))
    yield created

  def cancelSubscription(subscriptionId: SubscriptionId): IO[AppError, Subscription] =
    for
      sub     <- getSubscription(subscriptionId)
      _       <- ZIO.when(sub.status != SubscriptionStatus.Active)(
                   ZIO.fail(AppError.ValidationError("Subscription is not active")))
      updated  = sub.copy(status = SubscriptionStatus.Cancelled)
      result  <- liftTask(subscriptionRepo.update(updated))
    yield result

  def getUserSubscriptions(userId: UserId): IO[AppError, List[Subscription]] =
    liftTask(subscriptionRepo.findByUser(userId))

  def getSubscription(id: SubscriptionId): IO[AppError, Subscription] =
    liftTask(subscriptionRepo.findById(id))
      .flatMap(_.fold(ZIO.fail(AppError.NotFound("Subscription", id.toString)))(ZIO.succeed(_)))

object SubscriptionServiceLive:
  val layer: URLayer[SubscriptionPlanRepository & SubscriptionRepository & PaymentGateway, SubscriptionService] = ZLayer {
    for
      planRepo         <- ZIO.service[SubscriptionPlanRepository]
      subscriptionRepo <- ZIO.service[SubscriptionRepository]
      paymentGateway   <- ZIO.service[PaymentGateway]
    yield SubscriptionServiceLive(planRepo, subscriptionRepo, paymentGateway)
  }

// ─── Waitlist Service ──────────────────────────────────────
trait WaitlistService:
  def joinWaitlist(userId: UserId, scheduledSessionId: ScheduledSessionId): IO[AppError, WaitlistEntry]
  def leaveWaitlist(userId: UserId, scheduledSessionId: ScheduledSessionId): IO[AppError, Unit]
  def getWaitlist(scheduledSessionId: ScheduledSessionId): IO[AppError, List[WaitlistEntry]]
  def getUserWaitlistEntries(userId: UserId): IO[AppError, List[WaitlistEntry]]
  def promoteNext(scheduledSessionId: ScheduledSessionId): IO[AppError, Option[Booking]]

object WaitlistService:
  def joinWaitlist(userId: UserId, scheduledSessionId: ScheduledSessionId): ZIO[WaitlistService, AppError, WaitlistEntry] =
    ZIO.serviceWithZIO[WaitlistService](_.joinWaitlist(userId, scheduledSessionId))
  def leaveWaitlist(userId: UserId, scheduledSessionId: ScheduledSessionId): ZIO[WaitlistService, AppError, Unit] =
    ZIO.serviceWithZIO[WaitlistService](_.leaveWaitlist(userId, scheduledSessionId))
  def getWaitlist(scheduledSessionId: ScheduledSessionId): ZIO[WaitlistService, AppError, List[WaitlistEntry]] =
    ZIO.serviceWithZIO[WaitlistService](_.getWaitlist(scheduledSessionId))
  def getUserWaitlistEntries(userId: UserId): ZIO[WaitlistService, AppError, List[WaitlistEntry]] =
    ZIO.serviceWithZIO[WaitlistService](_.getUserWaitlistEntries(userId))
  def promoteNext(scheduledSessionId: ScheduledSessionId): ZIO[WaitlistService, AppError, Option[Booking]] =
    ZIO.serviceWithZIO[WaitlistService](_.promoteNext(scheduledSessionId))

case class WaitlistServiceLive(
  waitlistRepo: WaitlistRepository,
  bookingRepo: BookingRepository,
  scheduledSessionRepo: ScheduledSessionRepository,
  sessionRepo: SessionRepository
) extends WaitlistService:

  def joinWaitlist(userId: UserId, scheduledSessionId: ScheduledSessionId): IO[AppError, WaitlistEntry] =
    for
      existing <- liftTask(waitlistRepo.findByUserAndSession(userId, scheduledSessionId))
      _        <- ZIO.when(existing.isDefined)(ZIO.fail(AppError.ValidationError("Already on waitlist")))
      entries  <- liftTask(waitlistRepo.findByScheduledSession(scheduledSessionId))
      now      <- Clock.instant
      entry     = WaitlistEntry(UUID.randomUUID(), userId, scheduledSessionId, entries.size + 1, now)
      created  <- liftTask(waitlistRepo.create(entry))
    yield created

  def leaveWaitlist(userId: UserId, scheduledSessionId: ScheduledSessionId): IO[AppError, Unit] =
    liftTask(waitlistRepo.removeByUserAndSession(userId, scheduledSessionId)).unit

  def getWaitlist(scheduledSessionId: ScheduledSessionId): IO[AppError, List[WaitlistEntry]] =
    liftTask(waitlistRepo.findByScheduledSession(scheduledSessionId)).map(_.sortBy(_.position))

  def getUserWaitlistEntries(userId: UserId): IO[AppError, List[WaitlistEntry]] =
    liftTask(waitlistRepo.findByUser(userId))

  def promoteNext(scheduledSessionId: ScheduledSessionId): IO[AppError, Option[Booking]] =
    for
      entries  <- getWaitlist(scheduledSessionId)
      result   <- entries.headOption match
        case None => ZIO.succeed(None)
        case Some(entry) =>
          for
            scheduled <- liftTask(scheduledSessionRepo.findById(scheduledSessionId))
                           .flatMap(_.fold(ZIO.fail(AppError.NotFound("ScheduledSession", scheduledSessionId.toString)))(ZIO.succeed(_)))
            session   <- liftTask(sessionRepo.findById(scheduled.sessionId))
                           .flatMap(_.fold(ZIO.fail(AppError.NotFound("Session", scheduled.sessionId.toString)))(ZIO.succeed(_)))
            _         <- ZIO.when(scheduled.currentBookings >= session.capacity)(ZIO.fail(AppError.CapacityExceeded(scheduledSessionId.toString)))
            now       <- Clock.instant
            booking    = Booking(UUID.randomUUID(), entry.userId, scheduledSessionId, BookingStatus.Confirmed, None, now)
            created   <- liftTask(bookingRepo.create(booking))
            _         <- liftTask(scheduledSessionRepo.update(scheduled.copy(currentBookings = scheduled.currentBookings + 1)))
            _         <- liftTask(waitlistRepo.delete(entry.id))
          yield Some(created)
    yield result

object WaitlistServiceLive:
  val layer: URLayer[WaitlistRepository & BookingRepository & ScheduledSessionRepository & SessionRepository, WaitlistService] = ZLayer {
    for
      waitlistRepo  <- ZIO.service[WaitlistRepository]
      bookingRepo   <- ZIO.service[BookingRepository]
      scheduledRepo <- ZIO.service[ScheduledSessionRepository]
      sessionRepo   <- ZIO.service[SessionRepository]
    yield WaitlistServiceLive(waitlistRepo, bookingRepo, scheduledRepo, sessionRepo)
  }
