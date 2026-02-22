package com.bookingplatform.core.identity

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.{UserRole, UserStatus}
import zio.*

import java.time.Instant

final case class UserService(userRepo: UserRepository):

  def createUser(
      email: Email,
      firstName: NonEmptyString,
      lastName: NonEmptyString,
      phone: Option[Phone],
      role: UserRole
  ): IO[DomainError, User] =
    for
      existing <- userRepo.findByEmail(email)
      _        <- ZIO.when(existing.isDefined)(ZIO.fail(DomainError.ConflictError(s"User with email already exists")))
      now       = Instant.now()
      user = User(
        id = UserId.generate,
        email = email,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        role = role,
        status = UserStatus.Active,
        createdAt = now,
        updatedAt = now
      )
      created <- userRepo.create(user)
    yield created

  def getUser(id: UserId): IO[DomainError, User] =
    userRepo.findById(id).flatMap {
      case Some(user) => ZIO.succeed(user)
      case None       => ZIO.fail(DomainError.notFound("User", id.value))
    }

  def updateUser(
      id: UserId,
      firstName: Option[NonEmptyString],
      lastName: Option[NonEmptyString],
      phone: Option[Phone],
      status: Option[UserStatus]
  ): IO[DomainError, User] =
    for
      user <- getUser(id)
      updated = user.copy(
        firstName = firstName.getOrElse(user.firstName),
        lastName = lastName.getOrElse(user.lastName),
        phone = phone.orElse(user.phone),
        status = status.getOrElse(user.status),
        updatedAt = Instant.now()
      )
      result <- userRepo.update(updated)
    yield result

  def listUsers: IO[DomainError, List[User]] = userRepo.findAll

  def listTrainers: IO[DomainError, List[User]] = userRepo.findByRole(UserRole.Trainer)

object UserService:
  val layer: URLayer[UserRepository, UserService] =
    ZLayer.fromFunction(UserService.apply)
