package com.booking.application

import com.booking.domain.model.*
import com.booking.domain.repository.*
import com.booking.domain.service.BookingDomainService
import zio.*

case class CreateBookingCommand(
  memberId: MemberId,
  classSessionId: ClassSessionId,
)

case class BookingApplicationService(
  bookingRepo: BookingRepository,
  memberRepo: MemberRepository,
  sessionRepo: ClassSessionRepository,
):

  def createBooking(cmd: CreateBookingCommand): Task[Booking] =
    for
      member <- memberRepo.findById(cmd.memberId).flatMap {
                  case Some(m) => ZIO.succeed(m)
                  case None    => ZIO.fail(new NoSuchElementException(s"Member not found: ${cmd.memberId}"))
                }
      session <- sessionRepo.findById(cmd.classSessionId).flatMap {
                   case Some(s) => ZIO.succeed(s)
                   case None    => ZIO.fail(new NoSuchElementException(s"Session not found: ${cmd.classSessionId}"))
                 }
      existing <- bookingRepo.findActiveByMemberAndSession(cmd.memberId, cmd.classSessionId)
      _ <- ZIO.fromEither(
             BookingDomainService.validateBooking(member, session, existing)
               .left.map(e => new IllegalStateException(s"Booking validation failed: $e"))
           )
      booking   = Booking.create(cmd.memberId, cmd.classSessionId)
      saved     <- bookingRepo.save(booking)
      _         <- sessionRepo.update(session.incrementBooking())
    yield saved

  def cancelBooking(bookingId: BookingId, memberId: MemberId): Task[Booking] =
    for
      booking <- bookingRepo.findById(bookingId).flatMap {
                   case Some(b) => ZIO.succeed(b)
                   case None    => ZIO.fail(new NoSuchElementException(s"Booking not found: $bookingId"))
                 }
      _ <- ZIO.when(booking.memberId != memberId)(
             ZIO.fail(new SecurityException("Not authorized to cancel this booking"))
           )
      _ <- ZIO.fromEither(
             BookingDomainService.validateCancellation(booking)
               .left.map(e => new IllegalStateException(s"Cannot cancel: $e"))
           )
      cancelled <- bookingRepo.update(booking.cancel())
      session   <- sessionRepo.findById(booking.classSessionId).flatMap {
                     case Some(s) => ZIO.succeed(s)
                     case None    => ZIO.fail(new NoSuchElementException(s"Session not found: ${booking.classSessionId}"))
                   }
      _ <- sessionRepo.update(session.decrementBooking())
    yield cancelled

  def getMemberBookings(memberId: MemberId): Task[List[Booking]] =
    bookingRepo.findByMemberId(memberId)

  def getBooking(id: BookingId): Task[Option[Booking]] =
    bookingRepo.findById(id)

object BookingApplicationService:
  val layer: ZLayer[BookingRepository & MemberRepository & ClassSessionRepository, Nothing, BookingApplicationService] =
    ZLayer.fromFunction(BookingApplicationService.apply)
