package com.bookingplatform.infrastructure.restapi.controllers

import com.bookingplatform.api.endpoints.UserEndpoints
import com.bookingplatform.api.errors.{ApiError, ErrorMapping}
import com.bookingplatform.core.common.*
import com.bookingplatform.core.identity.UserService
import com.bookingplatform.infrastructure.restapi.controllers.ValidationHelper.validate
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*
import sttp.tapir.ztapir.*
import zio.*

object UserController:

  def endpoints(userService: UserService) = List(
    createUser(userService),
    getUser(userService),
    listUsers(userService),
    updateUser(userService)
  )

  private def toResponse(u: com.bookingplatform.core.identity.User): UserResponse =
    UserResponse(u.id.value, Email.unwrap(u.email), NonEmptyString.unwrap(u.firstName),
      NonEmptyString.unwrap(u.lastName), u.phone.map(Phone.unwrap), u.role, u.status,
      u.createdAt, u.updatedAt)

  private def createUser(svc: UserService) =
    UserEndpoints.createUser.zServerLogic { req =>
      (for
        email     <- validate(Email.make(req.email))
        firstName <- validate(NonEmptyString.make(req.firstName))
        lastName  <- validate(NonEmptyString.make(req.lastName))
        phone     <- ZIO.foreach(req.phone)(p => validate(Phone.make(p)))
        user      <- svc.createUser(email, firstName, lastName, phone, req.role)
      yield toResponse(user)).mapError(ErrorMapping.toApiError)
    }

  private def getUser(svc: UserService) =
    UserEndpoints.getUser.zServerLogic { userId =>
      svc.getUser(UserId(userId)).map(toResponse).mapError(ErrorMapping.toApiError)
    }

  private def listUsers(svc: UserService) =
    UserEndpoints.listUsers.zServerLogic { _ =>
      svc.listUsers.map(_.map(toResponse)).mapError(ErrorMapping.toApiError)
    }

  private def updateUser(svc: UserService) =
    UserEndpoints.updateUser.zServerLogic { case (userId, req) =>
      (for
        firstName <- ZIO.foreach(req.firstName)(n => validate(NonEmptyString.make(n)))
        lastName  <- ZIO.foreach(req.lastName)(n => validate(NonEmptyString.make(n)))
        phone     <- ZIO.foreach(req.phone)(p => validate(Phone.make(p)))
        user      <- svc.updateUser(UserId(userId), firstName, lastName, phone, req.status)
      yield toResponse(user)).mapError(ErrorMapping.toApiError)
    }
