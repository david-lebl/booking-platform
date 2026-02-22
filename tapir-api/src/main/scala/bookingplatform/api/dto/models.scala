package bookingplatform.api.dto

import zio.json.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*

// ─── Error ────────────────────
case class ErrorResponse(code: String, message: String)
object ErrorResponse:
  given JsonCodec[ErrorResponse] = DeriveJsonCodec.gen[ErrorResponse]

// ─── User ────────────────────
case class CreateUserRequest(name: String, email: String, phone: Option[String])
object CreateUserRequest:
  given JsonCodec[CreateUserRequest] = DeriveJsonCodec.gen[CreateUserRequest]

case class UpdateUserRequest(name: String, email: String, phone: Option[String])
object UpdateUserRequest:
  given JsonCodec[UpdateUserRequest] = DeriveJsonCodec.gen[UpdateUserRequest]

case class UserResponse(id: String, name: String, email: String, phone: Option[String], createdAt: String)
object UserResponse:
  given JsonCodec[UserResponse] = DeriveJsonCodec.gen[UserResponse]

// ─── Trainer ────────────────────
case class CreateTrainerRequest(name: String, email: String, specializations: List[String], bio: Option[String])
object CreateTrainerRequest:
  given JsonCodec[CreateTrainerRequest] = DeriveJsonCodec.gen[CreateTrainerRequest]

case class TrainerResponse(id: String, name: String, email: String, specializations: List[String], bio: Option[String])
object TrainerResponse:
  given JsonCodec[TrainerResponse] = DeriveJsonCodec.gen[TrainerResponse]

// ─── Session ────────────────────
case class CreateSessionRequest(
  name: String,
  description: Option[String],
  sessionType: String,
  trainerId: String,
  capacity: Int,
  durationMinutes: Int,
  price: Double,
  stationCount: Option[Int]
)
object CreateSessionRequest:
  given JsonCodec[CreateSessionRequest] = DeriveJsonCodec.gen[CreateSessionRequest]

case class SessionResponse(
  id: String,
  name: String,
  description: Option[String],
  sessionType: String,
  trainerId: String,
  capacity: Int,
  durationMinutes: Int,
  price: Double,
  stationCount: Option[Int]
)
object SessionResponse:
  given JsonCodec[SessionResponse] = DeriveJsonCodec.gen[SessionResponse]

// ─── Scheduled Session ────────────────────
case class ScheduleSessionRequest(sessionId: String, startTime: String)
object ScheduleSessionRequest:
  given JsonCodec[ScheduleSessionRequest] = DeriveJsonCodec.gen[ScheduleSessionRequest]

case class ScheduledSessionResponse(
  id: String,
  sessionId: String,
  startTime: String,
  endTime: String,
  currentBookings: Int,
  status: String
)
object ScheduledSessionResponse:
  given JsonCodec[ScheduledSessionResponse] = DeriveJsonCodec.gen[ScheduledSessionResponse]

// ─── Booking ────────────────────
case class CreateBookingRequest(userId: String, scheduledSessionId: String, subscriptionId: Option[String])
object CreateBookingRequest:
  given JsonCodec[CreateBookingRequest] = DeriveJsonCodec.gen[CreateBookingRequest]

case class CancelBookingRequest(reason: Option[String])
object CancelBookingRequest:
  given JsonCodec[CancelBookingRequest] = DeriveJsonCodec.gen[CancelBookingRequest]

case class BookingResponse(
  id: String,
  userId: String,
  scheduledSessionId: String,
  status: String,
  subscriptionId: Option[String],
  createdAt: String
)
object BookingResponse:
  given JsonCodec[BookingResponse] = DeriveJsonCodec.gen[BookingResponse]

case class CancellationResponse(
  id: String,
  bookingId: String,
  userId: String,
  reason: Option[String],
  lateFee: Double,
  cancelledAt: String
)
object CancellationResponse:
  given JsonCodec[CancellationResponse] = DeriveJsonCodec.gen[CancellationResponse]

// ─── Subscription Plan ────────────────────
case class CreateSubscriptionPlanRequest(
  name: String,
  planType: String,
  price: Double,
  sessionCount: Option[Int],
  validityDays: Option[Int],
  description: Option[String]
)
object CreateSubscriptionPlanRequest:
  given JsonCodec[CreateSubscriptionPlanRequest] = DeriveJsonCodec.gen[CreateSubscriptionPlanRequest]

case class SubscriptionPlanResponse(
  id: String,
  name: String,
  planType: String,
  price: Double,
  sessionCount: Option[Int],
  validityDays: Option[Int],
  description: Option[String]
)
object SubscriptionPlanResponse:
  given JsonCodec[SubscriptionPlanResponse] = DeriveJsonCodec.gen[SubscriptionPlanResponse]

// ─── Subscription ────────────────────
case class CreateSubscriptionRequest(userId: String, planId: String)
object CreateSubscriptionRequest:
  given JsonCodec[CreateSubscriptionRequest] = DeriveJsonCodec.gen[CreateSubscriptionRequest]

case class SubscriptionResponse(
  id: String,
  userId: String,
  planId: String,
  status: String,
  startDate: String,
  endDate: Option[String],
  remainingSessions: Option[Int],
  createdAt: String
)
object SubscriptionResponse:
  given JsonCodec[SubscriptionResponse] = DeriveJsonCodec.gen[SubscriptionResponse]

// ─── Waitlist ────────────────────
case class JoinWaitlistRequest(userId: String, scheduledSessionId: String)
object JoinWaitlistRequest:
  given JsonCodec[JoinWaitlistRequest] = DeriveJsonCodec.gen[JoinWaitlistRequest]

case class WaitlistEntryResponse(
  id: String,
  userId: String,
  scheduledSessionId: String,
  position: Int,
  createdAt: String
)
object WaitlistEntryResponse:
  given JsonCodec[WaitlistEntryResponse] = DeriveJsonCodec.gen[WaitlistEntryResponse]
