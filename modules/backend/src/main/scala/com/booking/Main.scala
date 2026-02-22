package com.booking

import com.booking.application.*
import com.booking.infrastructure.repository.*
import com.booking.presentation.*
import com.booking.presentation.api.*
import zio.*
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[ZIOAppArgs & ZEnvironment[Any] & Scope, Any, Any] =
    val host = "0.0.0.0"
    val port = 8080

    val program = for
      _           <- ZIO.logInfo(s"Starting Booking Platform on $host:$port")
      memberApi   <- ZIO.service[MemberApi]
      classApi    <- ZIO.service[ClassApi]
      bookingApi  <- ZIO.service[BookingApi]
      routes       = HttpServer.routes(memberApi, classApi, bookingApi)
      _           <- ZIO.logInfo("API docs available at http://localhost:8080/docs")
      _           <- HttpServer.start(host, port)(routes)
    yield ()

    program.provide(
      // Repositories
      InMemoryMemberRepository.layer,
      InMemoryInstructorRepository.layer,
      InMemoryClassTypeRepository.layer,
      InMemoryStudioRepository.layer,
      InMemoryClassSessionRepository.layer,
      InMemoryBookingRepository.layer,
      // Application services
      MemberApplicationService.layer,
      ClassApplicationService.layer,
      BookingApplicationService.layer,
      // API layers
      MemberApi.layer,
      ClassApi.layer,
      BookingApi.layer,
    )
