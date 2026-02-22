package com.bookingplatform.infrastructure.restapi.middleware

import com.bookingplatform.core.common.*
import com.bookingplatform.core.identity.{AuthService, Permission}
import com.bookingplatform.shared.ids.*
import zio.*

final case class StubAuthService() extends AuthService:
  def authenticate(token: String): IO[DomainError, UserId] =
    ZIO.attempt(UserId.fromString(token))
      .mapError(_ => DomainError.AuthenticationError("Invalid token format, expected UUID"))

  def authorize(userId: UserId, permission: Permission): IO[DomainError, Unit] =
    ZIO.unit // Always allow in dev mode

object StubAuthService:
  val layer: ULayer[AuthService] = ZLayer.succeed(StubAuthService())
