package com.bookingplatform.core.identity

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import zio.*

trait UserRepository:
  def create(user: User): IO[DomainError, User]
  def findById(id: UserId): IO[DomainError, Option[User]]
  def findByEmail(email: Email): IO[DomainError, Option[User]]
  def update(user: User): IO[DomainError, User]
  def findAll: IO[DomainError, List[User]]
  def findByRole(role: com.bookingplatform.shared.models.UserRole): IO[DomainError, List[User]]
