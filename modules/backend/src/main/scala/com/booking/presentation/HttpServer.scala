package com.booking.presentation

import com.booking.presentation.api.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.*
import zio.http.*

object HttpServer:

  def routes(
    memberApi: MemberApi,
    classApi: ClassApi,
    bookingApi: BookingApi,
  ): HttpApp[Any] =
    val allEndpoints = memberApi.routes ++ classApi.routes ++ bookingApi.routes
    val swaggerEndpoints = SwaggerInterpreter().fromServerEndpoints[Task](
      allEndpoints,
      "Booking Platform API",
      "1.0.0",
    )
    ZioHttpInterpreter().toHttp(allEndpoints ++ swaggerEndpoints)

  def start(host: String, port: Int)(routes: HttpApp[Any]): ZIO[Any, Throwable, Unit] =
    Server.serve(routes).provide(
      Server.defaultWithPort(port)
    )
