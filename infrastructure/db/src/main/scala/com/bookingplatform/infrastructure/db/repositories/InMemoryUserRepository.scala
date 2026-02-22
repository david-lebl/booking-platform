package com.bookingplatform.infrastructure.db.repositories

import com.bookingplatform.core.common.*
import com.bookingplatform.core.identity.{User, UserRepository}
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.UserRole
import zio.*

final case class InMemoryUserRepository(ref: Ref[Map[UserId, User]]) extends UserRepository:

  def create(user: User): IO[DomainError, User] =
    ref.modify { map =>
      if map.values.exists(u => Email.unwrap(u.email) == Email.unwrap(user.email))
      then (Left(DomainError.ConflictError("Email already exists")), map)
      else (Right(user), map + (user.id -> user))
    }.flatMap(ZIO.fromEither(_))

  def findById(id: UserId): IO[DomainError, Option[User]] =
    ref.get.map(_.get(id))

  def findByEmail(email: Email): IO[DomainError, Option[User]] =
    ref.get.map(_.values.find(u => Email.unwrap(u.email) == Email.unwrap(email)))

  def update(user: User): IO[DomainError, User] =
    ref.modify { map =>
      if map.contains(user.id)
      then (Right(user), map + (user.id -> user))
      else (Left(DomainError.notFound("User", user.id.value)), map)
    }.flatMap(ZIO.fromEither(_))

  def findAll: IO[DomainError, List[User]] =
    ref.get.map(_.values.toList)

  def findByRole(role: UserRole): IO[DomainError, List[User]] =
    ref.get.map(_.values.filter(_.role == role).toList)

object InMemoryUserRepository:
  val layer: ULayer[UserRepository] = ZLayer {
    Ref.make(Map.empty[UserId, User]).map(InMemoryUserRepository(_))
  }
