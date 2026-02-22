package com.booking.domain.service

import com.booking.domain.model.*

/** Pure domain service: encapsulates business rules for booking */
object BookingDomainService:

  sealed trait BookingError
  object BookingError:
    case object SessionNotBookable  extends BookingError
    case object AlreadyBooked       extends BookingError
    case object MemberNotActive     extends BookingError
    case object BookingNotFound     extends BookingError
    case object BookingAlreadyCancelled extends BookingError

  def validateBooking(
    member: Member,
    session: ClassSession,
    existingBooking: Option[Booking],
  ): Either[BookingError, Unit] =
    for
      _ <- Either.cond(member.active, (), BookingError.MemberNotActive)
      _ <- Either.cond(session.isBookable, (), BookingError.SessionNotBookable)
      _ <- Either.cond(existingBooking.isEmpty, (), BookingError.AlreadyBooked)
    yield ()

  def validateCancellation(booking: Booking): Either[BookingError, Unit] =
    if booking.isActive then Right(())
    else Left(BookingError.BookingAlreadyCancelled)
