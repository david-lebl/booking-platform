package com.bookingplatform.api.endpoints

import com.bookingplatform.api.codecs.TapirCodecs.given
import com.bookingplatform.api.errors.ApiError
import com.bookingplatform.shared.models.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import java.util.UUID

object UserEndpoints:

  val createUser: Endpoint[Unit, CreateUserRequest, ApiError, UserResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("users")
      .in(jsonBody[CreateUserRequest])
      .out(jsonBody[UserResponse])
      .tag("Users")

  val getUser: Endpoint[Unit, UUID, ApiError, UserResponse, Any] =
    BaseEndpoint.baseEndpoint.get
      .in("users" / path[UUID]("userId"))
      .out(jsonBody[UserResponse])
      .tag("Users")

  val listUsers: Endpoint[Unit, Unit, ApiError, List[UserResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("users")
      .out(jsonBody[List[UserResponse]])
      .tag("Users")

  val updateUser: Endpoint[Unit, (UUID, UpdateUserRequest), ApiError, UserResponse, Any] =
    BaseEndpoint.baseEndpoint.put
      .in("users" / path[UUID]("userId"))
      .in(jsonBody[UpdateUserRequest])
      .out(jsonBody[UserResponse])
      .tag("Users")
