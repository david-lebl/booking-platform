package bookingplatform

import bookingplatform.core.domain.*
import bookingplatform.core.service.*
import bookingplatform.core.port.CalendarService
import bookingplatform.infrastructure.repository.*
import bookingplatform.infrastructure.gateway.*
import zio.*
import zio.test.*
import zio.test.Assertion.*
import java.time.Instant
import java.time.temporal.ChronoUnit

object IntegrationSpec extends ZIOSpecDefault:

  private val testLayers = ZLayer.make[
    UserService & TrainerService & SessionService & BookingService & SubscriptionService & WaitlistService & CalendarService
  ](
    InMemoryUserRepository.layer,
    InMemoryTrainerRepository.layer,
    InMemorySessionRepository.layer,
    InMemoryScheduledSessionRepository.layer,
    InMemoryBookingRepository.layer,
    InMemorySubscriptionPlanRepository.layer,
    InMemorySubscriptionRepository.layer,
    InMemoryWaitlistRepository.layer,
    InMemoryCancellationRepository.layer,
    InMemoryPaymentRepository.layer,
    MockPaymentGateway.layer,
    MockNotificationService.layer,
    ICalCalendarService.layer,
    UserServiceLive.layer,
    TrainerServiceLive.layer,
    SessionServiceLive.layer,
    BookingServiceLive.layer,
    SubscriptionServiceLive.layer,
    WaitlistServiceLive.layer
  )

  def spec = suite("Booking Platform Integration Tests")(
    test("full booking flow: create user, trainer, session, schedule, and book") {
      for
        user      <- UserService.createUser("John Doe", "john@example.com", Some("+1234567890"))
        trainer   <- TrainerService.createTrainer("Jane Smith", "jane@example.com", List(SessionType.GroupTraining), Some("Yoga expert"))
        session   <- SessionService.createSession("Morning Yoga", Some("Relaxing yoga class"), SessionType.GroupTraining,
                       trainer.id, 10, 60, BigDecimal(25.0), None)
        startTime  = Instant.now().plus(2, ChronoUnit.DAYS)
        scheduled <- SessionService.scheduleSession(session.id, startTime)
        booking   <- BookingService.createBooking(user.id, scheduled.id, None)
      yield
        assertTrue(user.name == "John Doe") &&
        assertTrue(trainer.specializations == List(SessionType.GroupTraining)) &&
        assertTrue(session.capacity == 10) &&
        assertTrue(booking.status == BookingStatus.Confirmed) &&
        assertTrue(booking.userId == user.id) &&
        assertTrue(booking.scheduledSessionId == scheduled.id)
    },

    test("booking cancellation with late fee calculation") {
      for
        user      <- UserService.createUser("Cancel User", "cancel@example.com", None)
        trainer   <- TrainerService.createTrainer("Trainer Two", "t2@example.com", List(SessionType.IndividualTraining), None)
        session   <- SessionService.createSession("Personal Training", None, SessionType.IndividualTraining,
                       trainer.id, 1, 45, BigDecimal(50.0), None)
        startTime  = Instant.now().plus(1, ChronoUnit.HOURS) // less than 2 hours = full fee
        scheduled <- SessionService.scheduleSession(session.id, startTime)
        booking   <- BookingService.createBooking(user.id, scheduled.id, None)
        cancel    <- BookingService.cancelBooking(booking.id, Some("Emergency"))
      yield
        assertTrue(cancel.lateFee == BigDecimal(50.0)) &&
        assertTrue(cancel.reason == Some("Emergency"))
    },

    test("subscription flow: create plan, subscribe, book with subscription") {
      for
        user    <- UserService.createUser("Sub User", "sub@example.com", None)
        trainer <- TrainerService.createTrainer("Trainer Three", "t3@example.com", List(SessionType.ReformerPilates), None)
        session <- SessionService.createSession("Reformer Pilates", Some("Station-based class"), SessionType.ReformerPilates,
                     trainer.id, 6, 55, BigDecimal(35.0), Some(6))
        plan    <- SubscriptionService.createPlan("10-Pack", SubscriptionPlanType.Package, BigDecimal(300.0), Some(10), Some(90), Some("10 sessions in 90 days"))
        sub     <- SubscriptionService.subscribe(user.id, plan.id)
        _        = assertTrue(sub.status == SubscriptionStatus.Active)
        _        = assertTrue(sub.remainingSessions == Some(10))
        startTime = Instant.now().plus(3, ChronoUnit.DAYS)
        scheduled <- SessionService.scheduleSession(session.id, startTime)
        booking   <- BookingService.createBooking(user.id, scheduled.id, Some(sub.id))
        updatedSub <- SubscriptionService.getSubscription(sub.id)
      yield
        assertTrue(booking.status == BookingStatus.Confirmed) &&
        assertTrue(booking.subscriptionId == Some(sub.id)) &&
        assertTrue(updatedSub.remainingSessions == Some(9))
    },

    test("capacity check and duplicate booking prevention") {
      for
        user1     <- UserService.createUser("User One", "u1@example.com", None)
        user2     <- UserService.createUser("User Two", "u2@example.com", None)
        trainer   <- TrainerService.createTrainer("Solo Trainer", "solo@example.com", List(SessionType.IndividualTraining), None)
        session   <- SessionService.createSession("1-on-1 Session", None, SessionType.IndividualTraining,
                       trainer.id, 1, 30, BigDecimal(60.0), None)
        startTime  = Instant.now().plus(5, ChronoUnit.DAYS)
        scheduled <- SessionService.scheduleSession(session.id, startTime)
        booking1  <- BookingService.createBooking(user1.id, scheduled.id, None)
        booking2  <- BookingService.createBooking(user2.id, scheduled.id, None)
        dupResult <- BookingService.createBooking(user1.id, scheduled.id, None).either
      yield
        assertTrue(booking1.status == BookingStatus.Confirmed) &&
        assertTrue(booking2.status == BookingStatus.WaitListed) &&
        assertTrue(dupResult.isLeft)
    },

    test("waitlist operations") {
      for
        user1     <- UserService.createUser("Wait User 1", "w1@example.com", None)
        user2     <- UserService.createUser("Wait User 2", "w2@example.com", None)
        trainer   <- TrainerService.createTrainer("Wait Trainer", "wt@example.com", List(SessionType.GroupTraining), None)
        session   <- SessionService.createSession("Small Group", None, SessionType.GroupTraining,
                       trainer.id, 1, 45, BigDecimal(30.0), None)
        startTime  = Instant.now().plus(4, ChronoUnit.DAYS)
        scheduled <- SessionService.scheduleSession(session.id, startTime)
        _         <- BookingService.createBooking(user1.id, scheduled.id, None)
        entry     <- WaitlistService.joinWaitlist(user2.id, scheduled.id)
        entries   <- WaitlistService.getWaitlist(scheduled.id)
      yield
        assertTrue(entry.position == 1) &&
        assertTrue(entries.size == 1) &&
        assertTrue(entries.head.userId == user2.id)
    },

    test("iCal calendar generation") {
      for
        user      <- UserService.createUser("Cal User", "cal@example.com", None)
        trainer   <- TrainerService.createTrainer("Cal Trainer", "ct@example.com", List(SessionType.GroupTraining), None)
        session   <- SessionService.createSession("Evening Yoga", Some("Relaxing evening session"), SessionType.GroupTraining,
                       trainer.id, 20, 60, BigDecimal(20.0), None)
        startTime  = Instant.now().plus(6, ChronoUnit.DAYS)
        scheduled <- SessionService.scheduleSession(session.id, startTime)
        _         <- BookingService.createBooking(user.id, scheduled.id, None)
        bookings  <- BookingService.getUserBookings(user.id)
        confirmed  = bookings.filter(_.status == BookingStatus.Confirmed)
        events    <- ZIO.foreach(confirmed) { booking =>
          for
            s  <- SessionService.getScheduledSession(booking.scheduledSessionId)
            ss <- SessionService.getSession(s.sessionId)
          yield (ss, s, booking)
        }
        ical <- ZIO.serviceWithZIO[CalendarService](_.generateICalFeed(events))
      yield
        assertTrue(ical.contains("BEGIN:VCALENDAR")) &&
        assertTrue(ical.contains("Evening Yoga")) &&
        assertTrue(ical.contains("END:VCALENDAR"))
    },

    test("monthly subscription has no session limit") {
      for
        user    <- UserService.createUser("Monthly User", "monthly@example.com", None)
        plan    <- SubscriptionService.createPlan("Unlimited Monthly", SubscriptionPlanType.Monthly, BigDecimal(99.0), None, None, Some("Unlimited sessions"))
        sub     <- SubscriptionService.subscribe(user.id, plan.id)
      yield
        assertTrue(sub.status == SubscriptionStatus.Active) &&
        assertTrue(sub.remainingSessions.isEmpty) &&
        assertTrue(sub.endDate.isDefined)
    },

    test("cancel subscription") {
      for
        user      <- UserService.createUser("Cancel Sub User", "csub@example.com", None)
        plan      <- SubscriptionService.createPlan("Monthly Basic", SubscriptionPlanType.Monthly, BigDecimal(49.0), None, None, None)
        sub       <- SubscriptionService.subscribe(user.id, plan.id)
        cancelled <- SubscriptionService.cancelSubscription(sub.id)
      yield
        assertTrue(cancelled.status == SubscriptionStatus.Cancelled)
    }
  ).provideLayerShared(testLayers) @@ TestAspect.sequential @@ TestAspect.withLiveClock
