package com.bookingplatform.core.common

import java.util.UUID

enum DomainError:
  // Generic
  case NotFound(entityType: String, id: String)
  case ValidationError(message: String)
  case ConflictError(message: String)
  case AuthorizationError(message: String)
  case AuthenticationError(message: String)

  // Booking-specific
  case ClassFull(classInstanceId: UUID)
  case StationUnavailable(stationId: UUID)
  case BookingWindowClosed(message: String)
  case DuplicateBooking(userId: UUID, classInstanceId: UUID)
  case InvalidStateTransition(from: String, to: String)
  case CancellationNotAllowed(reason: String)
  case InsufficientCredits(required: Int, available: Int)

  // Billing-specific
  case PaymentFailed(reason: String)
  case SubscriptionNotActive(subscriptionId: UUID)
  case PackageExpired(packageId: UUID)

  // Infrastructure
  case DatabaseError(message: String)
  case ExternalServiceError(service: String, message: String)

object DomainError:
  def notFound(entityType: String, id: UUID): DomainError =
    NotFound(entityType, id.toString)
