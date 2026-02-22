package com.bookingplatform.infrastructure.restapi.routing

import com.bookingplatform.core.billing.BillingService
import com.bookingplatform.core.booking.{BookingService, WaitlistService}
import com.bookingplatform.core.catalog.CatalogService
import com.bookingplatform.core.identity.{AuthService, UserService}
import com.bookingplatform.core.scheduling.SchedulingService
import com.bookingplatform.infrastructure.restapi.controllers.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.*
import zio.*

object Routes:

  def makeRoutes(
      catalogService: CatalogService,
      userService: UserService,
      schedulingService: SchedulingService,
      bookingService: BookingService,
      waitlistService: WaitlistService,
      billingService: BillingService,
      authService: AuthService
  ) =
    val allEndpoints =
      CatalogController.endpoints(catalogService) ++
        UserController.endpoints(userService) ++
        SchedulingController.endpoints(schedulingService) ++
        BookingController.endpoints(bookingService, waitlistService, authService) ++
        BillingController.endpoints(billingService, authService)

    val swaggerEndpoints = SwaggerInterpreter()
      .fromServerEndpoints(allEndpoints, "Booking Platform API", "0.1.0")

    ZioHttpInterpreter().toHttp(allEndpoints ++ swaggerEndpoints)
