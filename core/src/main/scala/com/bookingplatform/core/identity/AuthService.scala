package com.bookingplatform.core.identity

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import zio.*

trait AuthService:
  def authenticate(token: String): IO[DomainError, UserId]
  def authorize(userId: UserId, permission: Permission): IO[DomainError, Unit]
