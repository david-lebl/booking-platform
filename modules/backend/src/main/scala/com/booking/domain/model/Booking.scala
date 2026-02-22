package com.booking.domain.model

import java.time.Instant

case class Booking(
  id: BookingId,
  memberId: MemberId,
  classSessionId: ClassSessionId,
  status: BookingStatus,
  createdAt: Instant,
  cancelledAt: Option[Instant],
):
  def cancel(): Booking = copy(status = BookingStatus.Cancelled, cancelledAt = Some(Instant.now()))
  def isActive: Boolean = status == BookingStatus.Confirmed || status == BookingStatus.WaitListed

object Booking:
  def create(memberId: MemberId, classSessionId: ClassSessionId): Booking =
    Booking(
      id = BookingId.generate(),
      memberId = memberId,
      classSessionId = classSessionId,
      status = BookingStatus.Confirmed,
      createdAt = Instant.now(),
      cancelledAt = None,
    )
