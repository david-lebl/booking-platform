package bookingplatform.ui.api

import org.scalajs.dom
import org.scalajs.dom.{Fetch, HttpMethod, RequestInit, Headers}
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON
import upickle.default.*
import scala.concurrent.ExecutionContext.Implicits.global

object ApiClient:
  private val baseUrl = "" // same origin, or "http://localhost:8080" for dev

  private def fetchJson[T: Reader](url: String): Future[T] =
    Fetch.fetch(s"$baseUrl$url").toFuture.flatMap { response =>
      if response.ok then
        response.text().toFuture.map(text => read[T](text))
      else
        response.text().toFuture.flatMap(text => Future.failed(new Exception(s"HTTP ${response.status}: $text")))
    }

  private def postJson[Req: Writer, Res: Reader](url: String, body: Req): Future[Res] =
    val headers = new Headers()
    headers.set("Content-Type", "application/json")
    val init = new RequestInit {}
    init.method = HttpMethod.POST
    init.headers = headers
    init.body = write(body)
    Fetch.fetch(s"$baseUrl$url", init).toFuture.flatMap { response =>
      if response.ok then
        response.text().toFuture.map(text => read[Res](text))
      else
        response.text().toFuture.flatMap(text => Future.failed(new Exception(s"HTTP ${response.status}: $text")))
    }

  private def putJson[Req: Writer, Res: Reader](url: String, body: Req): Future[Res] =
    val headers = new Headers()
    headers.set("Content-Type", "application/json")
    val init = new RequestInit {}
    init.method = HttpMethod.PUT
    init.headers = headers
    init.body = write(body)
    Fetch.fetch(s"$baseUrl$url", init).toFuture.flatMap { response =>
      if response.ok then
        response.text().toFuture.map(text => read[Res](text))
      else
        response.text().toFuture.flatMap(text => Future.failed(new Exception(s"HTTP ${response.status}: $text")))
    }

  private def deleteRequest(url: String): Future[Unit] =
    val init = new RequestInit {}
    init.method = HttpMethod.DELETE
    Fetch.fetch(s"$baseUrl$url", init).toFuture.flatMap { response =>
      if response.ok then Future.successful(())
      else response.text().toFuture.flatMap(text => Future.failed(new Exception(s"HTTP ${response.status}: $text")))
    }

  // ─── Users ────────────────────
  def getUsers(): Future[List[UserDto]] = fetchJson[List[UserDto]]("/api/users")
  def createUser(req: CreateUserReq): Future[UserDto] = postJson[CreateUserReq, UserDto]("/api/users", req)

  // ─── Trainers ────────────────────
  def getTrainers(): Future[List[TrainerDto]] = fetchJson[List[TrainerDto]]("/api/trainers")
  def createTrainer(req: CreateTrainerReq): Future[TrainerDto] = postJson[CreateTrainerReq, TrainerDto]("/api/trainers", req)

  // ─── Sessions ────────────────────
  def getSessions(): Future[List[SessionDto]] = fetchJson[List[SessionDto]]("/api/sessions")
  def createSession(req: CreateSessionReq): Future[SessionDto] = postJson[CreateSessionReq, SessionDto]("/api/sessions", req)
  def getUpcomingScheduled(): Future[List[ScheduledSessionDto]] = fetchJson[List[ScheduledSessionDto]]("/api/sessions/scheduled")
  def scheduleSession(req: ScheduleSessionReq): Future[ScheduledSessionDto] = postJson[ScheduleSessionReq, ScheduledSessionDto]("/api/sessions/schedule", req)

  // ─── Bookings ────────────────────
  def createBooking(req: CreateBookingReq): Future[BookingDto] = postJson[CreateBookingReq, BookingDto]("/api/bookings", req)
  def getUserBookings(userId: String): Future[List[BookingDto]] = fetchJson[List[BookingDto]](s"/api/users/$userId/bookings")
  def cancelBooking(bookingId: String, req: CancelBookingReq): Future[CancellationDto] =
    postJson[CancelBookingReq, CancellationDto](s"/api/bookings/$bookingId/cancel", req)

  // ─── Subscriptions ────────────────────
  def getSubscriptionPlans(): Future[List[SubscriptionPlanDto]] = fetchJson[List[SubscriptionPlanDto]]("/api/subscription-plans")
  def createSubscriptionPlan(req: CreateSubscriptionPlanReq): Future[SubscriptionPlanDto] =
    postJson[CreateSubscriptionPlanReq, SubscriptionPlanDto]("/api/subscription-plans", req)
  def subscribe(req: CreateSubscriptionReq): Future[SubscriptionDto] = postJson[CreateSubscriptionReq, SubscriptionDto]("/api/subscriptions", req)
  def getUserSubscriptions(userId: String): Future[List[SubscriptionDto]] = fetchJson[List[SubscriptionDto]](s"/api/users/$userId/subscriptions")

  // ─── Waitlist ────────────────────
  def joinWaitlist(req: JoinWaitlistReq): Future[WaitlistEntryDto] = postJson[JoinWaitlistReq, WaitlistEntryDto]("/api/waitlist", req)
  def getWaitlist(scheduledSessionId: String): Future[List[WaitlistEntryDto]] = fetchJson[List[WaitlistEntryDto]](s"/api/waitlist/$scheduledSessionId")


// ─── DTO case classes with upickle ReadWriter ────────────────────
case class UserDto(id: String, name: String, email: String, phone: Option[String], createdAt: String) derives ReadWriter
case class CreateUserReq(name: String, email: String, phone: Option[String]) derives ReadWriter
case class TrainerDto(id: String, name: String, email: String, specializations: List[String], bio: Option[String]) derives ReadWriter
case class CreateTrainerReq(name: String, email: String, specializations: List[String], bio: Option[String]) derives ReadWriter
case class SessionDto(id: String, name: String, description: Option[String], sessionType: String, trainerId: String, capacity: Int, durationMinutes: Int, price: Double, stationCount: Option[Int]) derives ReadWriter
case class CreateSessionReq(name: String, description: Option[String], sessionType: String, trainerId: String, capacity: Int, durationMinutes: Int, price: Double, stationCount: Option[Int]) derives ReadWriter
case class ScheduledSessionDto(id: String, sessionId: String, startTime: String, endTime: String, currentBookings: Int, status: String) derives ReadWriter
case class ScheduleSessionReq(sessionId: String, startTime: String) derives ReadWriter
case class BookingDto(id: String, userId: String, scheduledSessionId: String, status: String, subscriptionId: Option[String], createdAt: String) derives ReadWriter
case class CreateBookingReq(userId: String, scheduledSessionId: String, subscriptionId: Option[String]) derives ReadWriter
case class CancelBookingReq(reason: Option[String]) derives ReadWriter
case class CancellationDto(id: String, bookingId: String, userId: String, reason: Option[String], lateFee: Double, cancelledAt: String) derives ReadWriter
case class SubscriptionPlanDto(id: String, name: String, planType: String, price: Double, sessionCount: Option[Int], validityDays: Option[Int], description: Option[String]) derives ReadWriter
case class CreateSubscriptionPlanReq(name: String, planType: String, price: Double, sessionCount: Option[Int], validityDays: Option[Int], description: Option[String]) derives ReadWriter
case class SubscriptionDto(id: String, userId: String, planId: String, status: String, startDate: String, endDate: Option[String], remainingSessions: Option[Int], createdAt: String) derives ReadWriter
case class CreateSubscriptionReq(userId: String, planId: String) derives ReadWriter
case class JoinWaitlistReq(userId: String, scheduledSessionId: String) derives ReadWriter
case class WaitlistEntryDto(id: String, userId: String, scheduledSessionId: String, position: Int, createdAt: String) derives ReadWriter
