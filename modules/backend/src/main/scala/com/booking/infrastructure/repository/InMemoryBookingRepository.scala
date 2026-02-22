package com.booking.infrastructure.repository

import com.booking.domain.model.*
import com.booking.domain.repository.BookingRepository
import zio.*

final class InMemoryBookingRepository(ref: Ref[Map[BookingId, Booking]]) extends BookingRepository:

  def save(booking: Booking): Task[Booking] =
    ref.update(_ + (booking.id -> booking)).as(booking)

  def findById(id: BookingId): Task[Option[Booking]] =
    ref.get.map(_.get(id))

  def findByMemberId(memberId: MemberId): Task[List[Booking]] =
    ref.get.map(_.values.filter(_.memberId == memberId).toList)

  def findByClassSessionId(sessionId: ClassSessionId): Task[List[Booking]] =
    ref.get.map(_.values.filter(_.classSessionId == sessionId).toList)

  def findActiveByMemberAndSession(memberId: MemberId, sessionId: ClassSessionId): Task[Option[Booking]] =
    ref.get.map(_.values.find(b => b.memberId == memberId && b.classSessionId == sessionId && b.isActive))

  def update(booking: Booking): Task[Booking] =
    ref.update(_ + (booking.id -> booking)).as(booking)

object InMemoryBookingRepository:
  val layer: ZLayer[Any, Nothing, BookingRepository] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[BookingId, Booking]).map(new InMemoryBookingRepository(_))
    )
