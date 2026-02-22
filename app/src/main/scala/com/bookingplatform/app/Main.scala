package com.bookingplatform.app

import com.bookingplatform.core.billing.BillingService
import com.bookingplatform.core.booking.{BookingService, WaitlistService}
import com.bookingplatform.core.catalog.CatalogService
import com.bookingplatform.core.identity.{AuthService, UserService}
import com.bookingplatform.core.scheduling.SchedulingService
import com.bookingplatform.infrastructure.restapi.routing.Routes
import zio.*
import zio.http.*

object Main extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> zio.logging.consoleLogger()

  def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    val program =
      for
        config            <- ZIO.service[AppConfig]
        catalogService    <- ZIO.service[CatalogService]
        userService       <- ZIO.service[UserService]
        schedulingService <- ZIO.service[SchedulingService]
        bookingService    <- ZIO.service[BookingService]
        waitlistService   <- ZIO.service[WaitlistService]
        billingService    <- ZIO.service[BillingService]
        authService       <- ZIO.service[AuthService]

        routes = Routes.makeRoutes(
          catalogService, userService, schedulingService,
          bookingService, waitlistService, billingService, authService
        )

        _ <- ZIO.logInfo(s"Starting server on ${config.server.host}:${config.server.port}")
        _ <- ZIO.logInfo(s"Swagger UI available at http://${config.server.host}:${config.server.port}/docs")
        _ <- Server.serve(routes.asInstanceOf[zio.http.Routes[Any, zio.http.Response]]).provide(
          Server.defaultWith(_.binding(config.server.host, config.server.port))
        )
      yield ()

    program.provide(AppLayers.allLayers)
