package bookingplatform.infrastructure.gateway

import bookingplatform.core.domain.*
import bookingplatform.core.port.*
import zio.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

// ─── Mock Payment Gateway ────────────────────
case class MockPaymentGateway() extends PaymentGateway:
  def charge(userId: UserId, amount: BigDecimal, description: String): Task[Payment] =
    for
      now <- Clock.instant
      payment = Payment(
        id = UUID.randomUUID(),
        userId = userId,
        amount = amount,
        paymentType = PaymentType.BookingPayment,
        status = PaymentStatus.Completed,
        referenceId = description,
        createdAt = now
      )
      _ <- ZIO.logInfo(s"[MockPayment] Charged $$${amount} to user $userId: $description")
    yield payment

  def refund(paymentId: PaymentId, amount: BigDecimal): Task[Payment] =
    for
      now <- Clock.instant
      payment = Payment(
        id = UUID.randomUUID(),
        userId = UUID.randomUUID(),
        amount = amount,
        paymentType = PaymentType.Refund,
        status = PaymentStatus.Completed,
        referenceId = s"Refund for $paymentId",
        createdAt = now
      )
      _ <- ZIO.logInfo(s"[MockPayment] Refunded $$${amount} for payment $paymentId")
    yield payment

object MockPaymentGateway:
  val layer: ULayer[PaymentGateway] = ZLayer.succeed(MockPaymentGateway())

// ─── Mock Notification Service ────────────────────
case class MockNotificationService() extends NotificationService:
  def sendEmail(to: String, subject: String, body: String): Task[Unit] =
    ZIO.logInfo(s"[MockEmail] To: $to, Subject: $subject, Body: ${body.take(100)}...")

  def sendSms(to: String, message: String): Task[Unit] =
    ZIO.logInfo(s"[MockSMS] To: $to, Message: ${message.take(100)}...")

object MockNotificationService:
  val layer: ULayer[NotificationService] = ZLayer.succeed(MockNotificationService())

// ─── iCal Calendar Service ────────────────────
case class ICalCalendarService() extends CalendarService:
  private val dtFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
    .withZone(java.time.ZoneOffset.UTC)

  def generateICalEvent(session: Session, scheduled: ScheduledSession, booking: Booking): Task[String] =
    ZIO.succeed {
      val sb = new StringBuilder
      sb.append("BEGIN:VEVENT\r\n")
      sb.append(s"UID:${booking.id}@bookingplatform\r\n")
      sb.append(s"DTSTART:${dtFormatter.format(scheduled.startTime)}\r\n")
      sb.append(s"DTEND:${dtFormatter.format(scheduled.endTime)}\r\n")
      sb.append(s"SUMMARY:${session.name}\r\n")
      session.description.foreach(d => sb.append(s"DESCRIPTION:$d\r\n"))
      sb.append(s"STATUS:${if booking.status == BookingStatus.Confirmed then "CONFIRMED" else "TENTATIVE"}\r\n")
      sb.append("END:VEVENT\r\n")
      sb.toString
    }

  def generateICalFeed(events: List[(Session, ScheduledSession, Booking)]): Task[String] =
    for
      eventStrings <- ZIO.foreach(events) { case (session, scheduled, booking) =>
        generateICalEvent(session, scheduled, booking)
      }
    yield
      val sb = new StringBuilder
      sb.append("BEGIN:VCALENDAR\r\n")
      sb.append("VERSION:2.0\r\n")
      sb.append("PRODID:-//Booking Platform//EN\r\n")
      sb.append("CALSCALE:GREGORIAN\r\n")
      sb.append("METHOD:PUBLISH\r\n")
      eventStrings.foreach(sb.append)
      sb.append("END:VCALENDAR\r\n")
      sb.toString

object ICalCalendarService:
  val layer: ULayer[CalendarService] = ZLayer.succeed(ICalCalendarService())
