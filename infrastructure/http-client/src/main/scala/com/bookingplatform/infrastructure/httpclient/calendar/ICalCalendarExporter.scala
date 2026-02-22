package com.bookingplatform.infrastructure.httpclient.calendar

import com.bookingplatform.core.common.*
import com.bookingplatform.core.notification.CalendarExporter
import com.bookingplatform.shared.ids.*
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.*
import net.fortuna.ical4j.data.CalendarOutputter
import zio.*

import java.io.ByteArrayOutputStream
import java.time.Instant

final case class ICalCalendarExporter() extends CalendarExporter:

  def exportBooking(
      bookingId: BookingId,
      className: String,
      venueName: String,
      startTime: Instant,
      endTime: Instant
  ): IO[DomainError, Array[Byte]] =
    ZIO.attempt {
      val event = new VEvent(startTime, endTime, className)
      event.add(new Location(venueName))
      event.add(new Uid(bookingId.value.toString))

      val calendar = new Calendar()
      calendar.add(new ProdId("-//Booking Platform//EN"))
      calendar.add(event)

      val outputter = new CalendarOutputter()
      val baos      = new ByteArrayOutputStream()
      outputter.output(calendar, baos)
      baos.toByteArray
    }.mapError(e => DomainError.ExternalServiceError("iCal", e.getMessage))

object ICalCalendarExporter:
  val layer: ULayer[CalendarExporter] = ZLayer.succeed(ICalCalendarExporter())
