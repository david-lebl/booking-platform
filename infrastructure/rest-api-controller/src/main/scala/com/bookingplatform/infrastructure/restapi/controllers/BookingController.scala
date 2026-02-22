package com.bookingplatform.infrastructure.restapi.controllers

import com.bookingplatform.api.endpoints.BookingEndpoints
import com.bookingplatform.api.errors.{ApiError, ErrorMapping}
import com.bookingplatform.core.booking.{Booking, BookingService, WaitlistEntry, WaitlistService}
import com.bookingplatform.core.common.*
import com.bookingplatform.core.identity.AuthService
import com.bookingplatform.infrastructure.restapi.controllers.ValidationHelper.validate
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*
import sttp.tapir.ztapir.*
import zio.*

object BookingController:

  def endpoints(
      bookingService: BookingService,
      waitlistService: WaitlistService,
      authService: AuthService
  ) = List(
    createBooking(bookingService, authService),
    getBooking(bookingService),
    getUserBookings(bookingService, authService),
    cancelBooking(bookingService, authService),
    checkIn(bookingService),
    noShow(bookingService),
    joinWaitlist(waitlistService, authService),
    acceptWaitlistOffer(waitlistService, authService),
    getWaitlist(waitlistService),
    createCancellationPolicy(bookingService),
    listCancellationPolicies(bookingService)
  )

  private def toBookingResponse(b: Booking): BookingResponse =
    BookingResponse(b.id.value, b.userId.value, b.classInstanceId.value,
      b.stationId.map(_.value), b.status, b.cancellationFee, b.createdAt, b.updatedAt)

  private def toWaitlistResponse(w: WaitlistEntry): WaitlistEntryResponse =
    WaitlistEntryResponse(w.id.value, w.classInstanceId.value, w.userId.value,
      w.position, w.status, w.offerExpiresAt, w.createdAt)

  private def createBooking(svc: BookingService, auth: AuthService) =
    BookingEndpoints.createBooking
      .zServerSecurityLogic(token => auth.authenticate(token).mapError(ErrorMapping.toApiError))
      .serverLogic { userId => req =>
        svc.createBooking(userId, ClassInstanceId(req.classInstanceId), req.stationId.map(StationId(_)))
          .map(toBookingResponse)
          .mapError(ErrorMapping.toApiError)
      }

  private def getBooking(svc: BookingService) =
    BookingEndpoints.getBooking.zServerLogic { id =>
      svc.getBooking(BookingId(id)).map(toBookingResponse).mapError(ErrorMapping.toApiError)
    }

  private def getUserBookings(svc: BookingService, auth: AuthService) =
    BookingEndpoints.getUserBookings
      .zServerSecurityLogic(token => auth.authenticate(token).mapError(ErrorMapping.toApiError))
      .serverLogic { userId => _ =>
        svc.getUserBookings(userId).map(_.map(toBookingResponse)).mapError(ErrorMapping.toApiError)
      }

  private def cancelBooking(svc: BookingService, auth: AuthService) =
    BookingEndpoints.cancelBooking
      .zServerSecurityLogic(token => auth.authenticate(token).mapError(ErrorMapping.toApiError))
      .serverLogic { userId => bookingId =>
        svc.cancelBooking(BookingId(bookingId), userId).map(toBookingResponse).mapError(ErrorMapping.toApiError)
      }

  private def checkIn(svc: BookingService) =
    BookingEndpoints.checkInBooking.zServerLogic { id =>
      svc.checkIn(BookingId(id)).map(toBookingResponse).mapError(ErrorMapping.toApiError)
    }

  private def noShow(svc: BookingService) =
    BookingEndpoints.noShowBooking.zServerLogic { id =>
      svc.markNoShow(BookingId(id)).map(toBookingResponse).mapError(ErrorMapping.toApiError)
    }

  private def joinWaitlist(svc: WaitlistService, auth: AuthService) =
    BookingEndpoints.joinWaitlist
      .zServerSecurityLogic(token => auth.authenticate(token).mapError(ErrorMapping.toApiError))
      .serverLogic { userId => classInstanceId =>
        svc.joinWaitlist(userId, ClassInstanceId(classInstanceId))
          .map(toWaitlistResponse)
          .mapError(ErrorMapping.toApiError)
      }

  private def acceptWaitlistOffer(svc: WaitlistService, auth: AuthService) =
    BookingEndpoints.acceptWaitlistOffer
      .zServerSecurityLogic(token => auth.authenticate(token).mapError(ErrorMapping.toApiError))
      .serverLogic { _ => entryId =>
        svc.acceptOffer(WaitlistEntryId(entryId))
          .map(toWaitlistResponse)
          .mapError(ErrorMapping.toApiError)
      }

  private def getWaitlist(svc: WaitlistService) =
    BookingEndpoints.getWaitlist.zServerLogic { classInstanceId =>
      svc.getWaitlist(ClassInstanceId(classInstanceId))
        .map(_.map(toWaitlistResponse))
        .mapError(ErrorMapping.toApiError)
    }

  private def createCancellationPolicy(svc: BookingService) =
    BookingEndpoints.createCancellationPolicy.zServerLogic { req =>
      (for
        name <- validate(NonEmptyString.make(req.name))
        rules = req.rules.map(r => com.bookingplatform.core.booking.CancellationRule(
          r.noticePeriodMinutes, r.feeType, r.feeValue
        ))
        policy <- svc.createCancellationPolicy(name, rules)
      yield CancellationPolicyResponse(
        policy.id.value, NonEmptyString.unwrap(policy.name),
        policy.rules.map(r => CancellationRuleDto(r.noticePeriodMinutes, r.feeType, r.feeValue)),
        policy.createdAt
      )).mapError(ErrorMapping.toApiError)
    }

  private def listCancellationPolicies(svc: BookingService) =
    BookingEndpoints.listCancellationPolicies.zServerLogic { _ =>
      svc.listCancellationPolicies.map(_.map(p =>
        CancellationPolicyResponse(p.id.value, NonEmptyString.unwrap(p.name),
          p.rules.map(r => CancellationRuleDto(r.noticePeriodMinutes, r.feeType, r.feeValue)),
          p.createdAt)
      )).mapError(ErrorMapping.toApiError)
    }
