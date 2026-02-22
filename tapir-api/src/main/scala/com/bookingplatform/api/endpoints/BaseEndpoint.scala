package com.bookingplatform.api.endpoints

import com.bookingplatform.api.codecs.TapirCodecs.given
import com.bookingplatform.api.errors.{ApiError, ErrorMapping}
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

object BaseEndpoint:

  val baseEndpoint: Endpoint[Unit, Unit, ApiError, Unit, Any] =
    endpoint
      .errorOut(jsonBody[ApiError])
      .in("api" / "v1")

  val securedEndpoint: Endpoint[String, Unit, ApiError, Unit, Any] =
    baseEndpoint
      .securityIn(auth.bearer[String]())
