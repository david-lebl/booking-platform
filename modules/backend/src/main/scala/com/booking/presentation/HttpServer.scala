package com.booking.presentation

import com.booking.presentation.api.*
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.ztapir.*
import zio.*
import zio.http.*

object HttpServer:

  def routes(
    memberApi: MemberApi,
    classApi: ClassApi,
    bookingApi: BookingApi,
  ): Routes[Any, Response] =
    val allEndpoints: List[ZServerEndpoint[Any, Any]] =
      memberApi.routes ++ classApi.routes ++ bookingApi.routes
    val swaggerEndpoints: List[ZServerEndpoint[Any, Any]] =
      SwaggerInterpreter().fromServerEndpoints[Task](
        allEndpoints,
        "Booking Platform API",
        "1.0.0",
      )
    ZioHttpInterpreter().toHttp(allEndpoints ++ swaggerEndpoints)

  def start(host: String, port: Int)(routes: Routes[Any, Response]): ZIO[Any, Throwable, Unit] =
    Server.serve(routes).provide(
      Server.defaultWithPort(port)
    ).unit
