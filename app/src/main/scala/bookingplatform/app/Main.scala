package bookingplatform.app

import bookingplatform.core.service.*
import bookingplatform.infrastructure.repository.*
import bookingplatform.infrastructure.gateway.*
import bookingplatform.infrastructure.controller.Routes
import zio.*
import zio.http.*

object Main extends ZIOAppDefault:

  private val appLayers = ZLayer.make[
    UserService & TrainerService & SessionService & BookingService & SubscriptionService & WaitlistService & bookingplatform.core.port.CalendarService
  ](
    // Repositories
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
    // Gateways
    MockPaymentGateway.layer,
    MockNotificationService.layer,
    ICalCalendarService.layer,
    // Services
    UserServiceLive.layer,
    TrainerServiceLive.layer,
    SessionServiceLive.layer,
    BookingServiceLive.layer,
    SubscriptionServiceLive.layer,
    WaitlistServiceLive.layer
  )

  override val run =
    (for
      _ <- ZIO.logInfo("Starting Booking Platform API server on port 8080...")
      _ <- ZIO.logInfo("Swagger UI available at http://localhost:8080/docs")
      _ <- Server.serve(Routes.httpApp @@ Middleware.cors)
    yield ())
      .provide(
        Server.defaultWithPort(8080),
        appLayers
      )
