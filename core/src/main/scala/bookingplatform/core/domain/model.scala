package bookingplatform.core.domain

import java.time.{Instant, LocalDate}
import java.util.UUID

// Type aliases for domain IDs
type UserId = UUID
type TrainerId = UUID
type SessionId = UUID
type ScheduledSessionId = UUID
type BookingId = UUID
type SubscriptionPlanId = UUID
type SubscriptionId = UUID
type WaitlistEntryId = UUID
type PaymentId = UUID
type CancellationId = UUID

// Enumerations
enum SessionType:
  case IndividualTraining, GroupTraining, ReformerPilates

enum BookingStatus:
  case Confirmed, Cancelled, WaitListed, Completed, NoShow

enum SubscriptionPlanType:
  case OneTime, Package, Monthly

enum SubscriptionStatus:
  case Active, Expired, Cancelled, Paused

enum ScheduledSessionStatus:
  case Scheduled, InProgress, Completed, Cancelled

enum PaymentStatus:
  case Pending, Completed, Failed, Refunded

enum PaymentType:
  case BookingPayment, SubscriptionPayment, CancellationFee, Refund

// Entities
case class User(
  id: UserId,
  name: String,
  email: String,
  phone: Option[String],
  createdAt: Instant
)

case class Trainer(
  id: TrainerId,
  name: String,
  email: String,
  specializations: List[SessionType],
  bio: Option[String]
)

case class Session(
  id: SessionId,
  name: String,
  description: Option[String],
  sessionType: SessionType,
  trainerId: TrainerId,
  capacity: Int,
  durationMinutes: Int,
  price: BigDecimal,
  stationCount: Option[Int]
)

case class ScheduledSession(
  id: ScheduledSessionId,
  sessionId: SessionId,
  startTime: Instant,
  endTime: Instant,
  currentBookings: Int,
  status: ScheduledSessionStatus
)

case class Booking(
  id: BookingId,
  userId: UserId,
  scheduledSessionId: ScheduledSessionId,
  status: BookingStatus,
  subscriptionId: Option[SubscriptionId],
  createdAt: Instant
)

case class SubscriptionPlan(
  id: SubscriptionPlanId,
  name: String,
  planType: SubscriptionPlanType,
  price: BigDecimal,
  sessionCount: Option[Int],
  validityDays: Option[Int],
  description: Option[String]
)

case class Subscription(
  id: SubscriptionId,
  userId: UserId,
  planId: SubscriptionPlanId,
  status: SubscriptionStatus,
  startDate: LocalDate,
  endDate: Option[LocalDate],
  remainingSessions: Option[Int],
  createdAt: Instant
)

case class WaitlistEntry(
  id: WaitlistEntryId,
  userId: UserId,
  scheduledSessionId: ScheduledSessionId,
  position: Int,
  createdAt: Instant
)

case class Cancellation(
  id: CancellationId,
  bookingId: BookingId,
  userId: UserId,
  reason: Option[String],
  lateFee: BigDecimal,
  cancelledAt: Instant
)

case class Payment(
  id: PaymentId,
  userId: UserId,
  amount: BigDecimal,
  paymentType: PaymentType,
  status: PaymentStatus,
  referenceId: String,
  createdAt: Instant
)

// Domain Errors
enum AppError:
  case NotFound(entity: String, id: String)
  case ValidationError(message: String)
  case CapacityExceeded(sessionId: String)
  case AlreadyBooked(userId: String, sessionId: String)
  case InsufficientSessions(subscriptionId: String)
  case PaymentFailed(message: String)
  case Unauthorized(message: String)
  case InternalError(message: String)
