package bookingplatform.api.endpoint

import bookingplatform.api.dto.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import sttp.model.StatusCode

object Endpoints:

  // Base error output: (StatusCode, ErrorResponse)
  private val errorOutput = statusCode.and(jsonBody[ErrorResponse])

  // ─── User Endpoints ────────────────────
  val createUser = endpoint.tag("Users")
    .post.in("api" / "users")
    .in(jsonBody[CreateUserRequest])
    .out(jsonBody[UserResponse])
    .errorOut(errorOutput)

  val getUser = endpoint.tag("Users")
    .get.in("api" / "users" / path[String]("id"))
    .out(jsonBody[UserResponse])
    .errorOut(errorOutput)

  val getAllUsers = endpoint.tag("Users")
    .get.in("api" / "users")
    .out(jsonBody[List[UserResponse]])
    .errorOut(errorOutput)

  val updateUser = endpoint.tag("Users")
    .put.in("api" / "users" / path[String]("id"))
    .in(jsonBody[UpdateUserRequest])
    .out(jsonBody[UserResponse])
    .errorOut(errorOutput)

  // ─── Trainer Endpoints ────────────────────
  val createTrainer = endpoint.tag("Trainers")
    .post.in("api" / "trainers")
    .in(jsonBody[CreateTrainerRequest])
    .out(jsonBody[TrainerResponse])
    .errorOut(errorOutput)

  val getTrainer = endpoint.tag("Trainers")
    .get.in("api" / "trainers" / path[String]("id"))
    .out(jsonBody[TrainerResponse])
    .errorOut(errorOutput)

  val getAllTrainers = endpoint.tag("Trainers")
    .get.in("api" / "trainers")
    .out(jsonBody[List[TrainerResponse]])
    .errorOut(errorOutput)

  // ─── Session Endpoints ────────────────────
  val createSession = endpoint.tag("Sessions")
    .post.in("api" / "sessions")
    .in(jsonBody[CreateSessionRequest])
    .out(jsonBody[SessionResponse])
    .errorOut(errorOutput)

  val getSession = endpoint.tag("Sessions")
    .get.in("api" / "sessions" / path[String]("id"))
    .out(jsonBody[SessionResponse])
    .errorOut(errorOutput)

  val getAllSessions = endpoint.tag("Sessions")
    .get.in("api" / "sessions")
    .out(jsonBody[List[SessionResponse]])
    .errorOut(errorOutput)

  val scheduleSession = endpoint.tag("Sessions")
    .post.in("api" / "sessions" / "schedule")
    .in(jsonBody[ScheduleSessionRequest])
    .out(jsonBody[ScheduledSessionResponse])
    .errorOut(errorOutput)

  val getScheduledSession = endpoint.tag("Sessions")
    .get.in("api" / "sessions" / "scheduled" / path[String]("id"))
    .out(jsonBody[ScheduledSessionResponse])
    .errorOut(errorOutput)

  val getUpcomingScheduledSessions = endpoint.tag("Sessions")
    .get.in("api" / "sessions" / "scheduled")
    .out(jsonBody[List[ScheduledSessionResponse]])
    .errorOut(errorOutput)

  // ─── Booking Endpoints ────────────────────
  val createBooking = endpoint.tag("Bookings")
    .post.in("api" / "bookings")
    .in(jsonBody[CreateBookingRequest])
    .out(jsonBody[BookingResponse])
    .errorOut(errorOutput)

  val cancelBooking = endpoint.tag("Bookings")
    .post.in("api" / "bookings" / path[String]("id") / "cancel")
    .in(jsonBody[CancelBookingRequest])
    .out(jsonBody[CancellationResponse])
    .errorOut(errorOutput)

  val getBooking = endpoint.tag("Bookings")
    .get.in("api" / "bookings" / path[String]("id"))
    .out(jsonBody[BookingResponse])
    .errorOut(errorOutput)

  val getUserBookings = endpoint.tag("Bookings")
    .get.in("api" / "users" / path[String]("userId") / "bookings")
    .out(jsonBody[List[BookingResponse]])
    .errorOut(errorOutput)

  // ─── Subscription Endpoints ────────────────────
  val createSubscriptionPlan = endpoint.tag("Subscriptions")
    .post.in("api" / "subscription-plans")
    .in(jsonBody[CreateSubscriptionPlanRequest])
    .out(jsonBody[SubscriptionPlanResponse])
    .errorOut(errorOutput)

  val getSubscriptionPlans = endpoint.tag("Subscriptions")
    .get.in("api" / "subscription-plans")
    .out(jsonBody[List[SubscriptionPlanResponse]])
    .errorOut(errorOutput)

  val createSubscription = endpoint.tag("Subscriptions")
    .post.in("api" / "subscriptions")
    .in(jsonBody[CreateSubscriptionRequest])
    .out(jsonBody[SubscriptionResponse])
    .errorOut(errorOutput)

  val cancelSubscription = endpoint.tag("Subscriptions")
    .post.in("api" / "subscriptions" / path[String]("id") / "cancel")
    .out(jsonBody[SubscriptionResponse])
    .errorOut(errorOutput)

  val getUserSubscriptions = endpoint.tag("Subscriptions")
    .get.in("api" / "users" / path[String]("userId") / "subscriptions")
    .out(jsonBody[List[SubscriptionResponse]])
    .errorOut(errorOutput)

  val getSubscription = endpoint.tag("Subscriptions")
    .get.in("api" / "subscriptions" / path[String]("id"))
    .out(jsonBody[SubscriptionResponse])
    .errorOut(errorOutput)

  // ─── Waitlist Endpoints ────────────────────
  val joinWaitlist = endpoint.tag("Waitlist")
    .post.in("api" / "waitlist")
    .in(jsonBody[JoinWaitlistRequest])
    .out(jsonBody[WaitlistEntryResponse])
    .errorOut(errorOutput)

  val leaveWaitlist = endpoint.tag("Waitlist")
    .delete.in("api" / "waitlist" / path[String]("userId") / path[String]("scheduledSessionId"))
    .out(emptyOutput)
    .errorOut(errorOutput)

  val getWaitlist = endpoint.tag("Waitlist")
    .get.in("api" / "waitlist" / path[String]("scheduledSessionId"))
    .out(jsonBody[List[WaitlistEntryResponse]])
    .errorOut(errorOutput)

  // ─── Calendar Endpoints ────────────────────
  val getUserCalendar = endpoint.tag("Calendar")
    .get.in("api" / "users" / path[String]("userId") / "calendar.ics")
    .out(stringBody)
    .errorOut(errorOutput)

  // Gather all endpoints for Swagger documentation
  val all = List(
    createUser, getUser, getAllUsers, updateUser,
    createTrainer, getTrainer, getAllTrainers,
    createSession, getSession, getAllSessions, scheduleSession, getScheduledSession, getUpcomingScheduledSessions,
    createBooking, cancelBooking, getBooking, getUserBookings,
    createSubscriptionPlan, getSubscriptionPlans, createSubscription, cancelSubscription, getUserSubscriptions, getSubscription,
    joinWaitlist, leaveWaitlist, getWaitlist,
    getUserCalendar
  )
