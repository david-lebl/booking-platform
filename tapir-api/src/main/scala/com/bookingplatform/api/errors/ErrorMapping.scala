package com.bookingplatform.api.errors

import com.bookingplatform.core.common.DomainError
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import zio.json.*

final case class ApiError(
    code: String,
    message: String
) derives JsonEncoder,
      JsonDecoder

object ErrorMapping:

  def toStatusCode(error: DomainError): StatusCode = error match
    case _: DomainError.NotFound              => StatusCode.NotFound
    case _: DomainError.ValidationError       => StatusCode.BadRequest
    case _: DomainError.ConflictError         => StatusCode.Conflict
    case _: DomainError.AuthorizationError    => StatusCode.Forbidden
    case _: DomainError.AuthenticationError   => StatusCode.Unauthorized
    case _: DomainError.ClassFull             => StatusCode.Conflict
    case _: DomainError.StationUnavailable    => StatusCode.Conflict
    case _: DomainError.BookingWindowClosed   => StatusCode.BadRequest
    case _: DomainError.DuplicateBooking      => StatusCode.Conflict
    case _: DomainError.InvalidStateTransition => StatusCode.BadRequest
    case _: DomainError.CancellationNotAllowed => StatusCode.BadRequest
    case _: DomainError.InsufficientCredits   => StatusCode.PaymentRequired
    case _: DomainError.PaymentFailed         => StatusCode.PaymentRequired
    case _: DomainError.SubscriptionNotActive => StatusCode.BadRequest
    case _: DomainError.PackageExpired        => StatusCode.BadRequest
    case _: DomainError.DatabaseError         => StatusCode.InternalServerError
    case _: DomainError.ExternalServiceError  => StatusCode.BadGateway

  def toApiError(error: DomainError): ApiError = error match
    case DomainError.NotFound(entityType, id)   => ApiError("NOT_FOUND", s"$entityType with id $id not found")
    case DomainError.ValidationError(msg)       => ApiError("VALIDATION_ERROR", msg)
    case DomainError.ConflictError(msg)         => ApiError("CONFLICT", msg)
    case DomainError.AuthorizationError(msg)    => ApiError("FORBIDDEN", msg)
    case DomainError.AuthenticationError(msg)   => ApiError("UNAUTHORIZED", msg)
    case DomainError.ClassFull(id)              => ApiError("CLASS_FULL", s"Class instance $id is full")
    case DomainError.StationUnavailable(id)     => ApiError("STATION_UNAVAILABLE", s"Station $id is unavailable")
    case DomainError.BookingWindowClosed(msg)   => ApiError("BOOKING_WINDOW_CLOSED", msg)
    case DomainError.DuplicateBooking(uid, cid) => ApiError("DUPLICATE_BOOKING", s"User $uid already booked class $cid")
    case DomainError.InvalidStateTransition(f, t) => ApiError("INVALID_TRANSITION", s"Cannot transition from $f to $t")
    case DomainError.CancellationNotAllowed(r)  => ApiError("CANCELLATION_NOT_ALLOWED", r)
    case DomainError.InsufficientCredits(req, avail) => ApiError("INSUFFICIENT_CREDITS", s"Need $req credits, have $avail")
    case DomainError.PaymentFailed(reason)      => ApiError("PAYMENT_FAILED", reason)
    case DomainError.SubscriptionNotActive(id)  => ApiError("SUBSCRIPTION_NOT_ACTIVE", s"Subscription $id is not active")
    case DomainError.PackageExpired(id)         => ApiError("PACKAGE_EXPIRED", s"Package $id has expired")
    case DomainError.DatabaseError(msg)         => ApiError("INTERNAL_ERROR", "An internal error occurred")
    case DomainError.ExternalServiceError(s, m) => ApiError("EXTERNAL_SERVICE_ERROR", s"Error communicating with $s")

  val errorOutput: EndpointOutput[ApiError] =
    jsonBody[ApiError]
