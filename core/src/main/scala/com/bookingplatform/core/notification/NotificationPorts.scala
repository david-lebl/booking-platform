package com.bookingplatform.core.notification

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import zio.*

import java.time.Instant

final case class NotificationMessage(
    to: UserId,
    subject: String,
    body: String,
    channel: NotificationChannel
)

enum NotificationChannel:
  case Email, SMS, Push

trait NotificationSender:
  def send(message: NotificationMessage): IO[DomainError, Unit]

trait CalendarExporter:
  def exportBooking(
      bookingId: BookingId,
      className: String,
      venueName: String,
      startTime: Instant,
      endTime: Instant
  ): IO[DomainError, Array[Byte]]
