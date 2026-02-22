package bookingplatform.core.port

import bookingplatform.core.domain.*
import zio.*

trait PaymentGateway:
  def charge(userId: UserId, amount: BigDecimal, description: String): Task[Payment]
  def refund(paymentId: PaymentId, amount: BigDecimal): Task[Payment]

trait NotificationService:
  def sendEmail(to: String, subject: String, body: String): Task[Unit]
  def sendSms(to: String, message: String): Task[Unit]

trait CalendarService:
  def generateICalEvent(session: Session, scheduled: ScheduledSession, booking: Booking): Task[String]
  def generateICalFeed(events: List[(Session, ScheduledSession, Booking)]): Task[String]
