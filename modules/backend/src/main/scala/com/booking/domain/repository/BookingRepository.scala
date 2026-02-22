package com.booking.domain.repository

import com.booking.domain.model.*
import zio.*

trait BookingRepository:
  def save(booking: Booking): Task[Booking]
  def findById(id: BookingId): Task[Option[Booking]]
  def findByMemberId(memberId: MemberId): Task[List[Booking]]
  def findByClassSessionId(sessionId: ClassSessionId): Task[List[Booking]]
  def findActiveByMemberAndSession(memberId: MemberId, sessionId: ClassSessionId): Task[Option[Booking]]
  def update(booking: Booking): Task[Booking]

object BookingRepository:
  def save(booking: Booking): ZIO[BookingRepository, Throwable, Booking] =
    ZIO.serviceWithZIO(_.save(booking))
  def findById(id: BookingId): ZIO[BookingRepository, Throwable, Option[Booking]] =
    ZIO.serviceWithZIO(_.findById(id))
  def findByMemberId(memberId: MemberId): ZIO[BookingRepository, Throwable, List[Booking]] =
    ZIO.serviceWithZIO(_.findByMemberId(memberId))
  def findActiveByMemberAndSession(memberId: MemberId, sessionId: ClassSessionId): ZIO[BookingRepository, Throwable, Option[Booking]] =
    ZIO.serviceWithZIO(_.findActiveByMemberAndSession(memberId, sessionId))
  def update(booking: Booking): ZIO[BookingRepository, Throwable, Booking] =
    ZIO.serviceWithZIO(_.update(booking))
