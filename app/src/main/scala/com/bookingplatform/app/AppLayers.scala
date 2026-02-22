package com.bookingplatform.app

import com.bookingplatform.core.billing.*
import com.bookingplatform.core.booking.*
import com.bookingplatform.core.catalog.*
import com.bookingplatform.core.common.EventBus
import com.bookingplatform.core.identity.*
import com.bookingplatform.core.scheduling.*
import com.bookingplatform.infrastructure.db.repositories.*
import com.bookingplatform.infrastructure.payment.NoOpPaymentGateway
import com.bookingplatform.infrastructure.restapi.middleware.StubAuthService
import zio.*

object AppLayers:

  val repositoryLayers =
    InMemoryUserRepository.layer ++
      InMemoryVenueRepository.layer ++
      InMemoryRoomRepository.layer ++
      InMemoryStationRepository.layer ++
      InMemoryServiceDefinitionRepository.layer ++
      InMemoryClassDefinitionRepository.layer ++
      (InMemoryClassDefinitionRepository.layer >>> InMemoryClassInstanceRepository.layer) ++
      InMemoryWeeklyScheduleRepository.layer ++
      InMemoryBookingRepository.layer ++
      InMemoryWaitlistRepository.layer ++
      InMemoryCancellationPolicyRepository.layer ++
      InMemorySubscriptionPlanRepository.layer ++
      InMemorySubscriptionRepository.layer ++
      InMemoryPackageDefinitionRepository.layer ++
      InMemoryCreditPackageRepository.layer

  val infrastructureLayers =
    NoOpPaymentGateway.layer ++ StubAuthService.layer ++ EventBus.inMemory

  val catalogServiceLayer = CatalogService.layer
  val userServiceLayer     = UserService.layer
  val schedulingServiceLayer = catalogServiceLayer >>> SchedulingService.layer

  val bookingServiceLayer =
    (catalogServiceLayer ++ (catalogServiceLayer >>> SchedulingService.layer)) >>> BookingService.layer

  val waitlistServiceLayer = WaitlistService.layer
  val billingServiceLayer  = BillingService.layer

  val allLayers: ZLayer[Any, Nothing, UserService & CatalogService & SchedulingService & BookingService & WaitlistService & BillingService & AuthService & AppConfig] =
    val base = repositoryLayers ++ infrastructureLayers
    base >>> (
      userServiceLayer ++
        catalogServiceLayer ++
        schedulingServiceLayer ++
        bookingServiceLayer ++
        waitlistServiceLayer ++
        billingServiceLayer ++
        ZLayer.service[AuthService] ++
        AppConfig.layer
    )
