package com.bookingplatform.api.endpoints

import com.bookingplatform.api.codecs.TapirCodecs.given
import com.bookingplatform.api.errors.ApiError
import com.bookingplatform.shared.models.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import java.util.UUID

object BookingEndpoints:

  val createBooking: Endpoint[String, CreateBookingRequest, ApiError, BookingResponse, Any] =
    BaseEndpoint.securedEndpoint.post
      .in("bookings")
      .in(jsonBody[CreateBookingRequest])
      .out(jsonBody[BookingResponse])
      .tag("Bookings")

  val getBooking: Endpoint[Unit, UUID, ApiError, BookingResponse, Any] =
    BaseEndpoint.baseEndpoint.get
      .in("bookings" / path[UUID]("bookingId"))
      .out(jsonBody[BookingResponse])
      .tag("Bookings")

  val getUserBookings: Endpoint[String, Unit, ApiError, List[BookingResponse], Any] =
    BaseEndpoint.securedEndpoint.get
      .in("bookings" / "my")
      .out(jsonBody[List[BookingResponse]])
      .tag("Bookings")

  val cancelBooking: Endpoint[String, UUID, ApiError, BookingResponse, Any] =
    BaseEndpoint.securedEndpoint.post
      .in("bookings" / path[UUID]("bookingId") / "cancel")
      .out(jsonBody[BookingResponse])
      .tag("Bookings")

  val checkInBooking: Endpoint[Unit, UUID, ApiError, BookingResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("bookings" / path[UUID]("bookingId") / "check-in")
      .out(jsonBody[BookingResponse])
      .tag("Bookings")

  val noShowBooking: Endpoint[Unit, UUID, ApiError, BookingResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("bookings" / path[UUID]("bookingId") / "no-show")
      .out(jsonBody[BookingResponse])
      .tag("Bookings")

  // --- Waitlist ---

  val joinWaitlist: Endpoint[String, UUID, ApiError, WaitlistEntryResponse, Any] =
    BaseEndpoint.securedEndpoint.post
      .in("class-instances" / path[UUID]("classInstanceId") / "waitlist")
      .out(jsonBody[WaitlistEntryResponse])
      .tag("Waitlist")

  val acceptWaitlistOffer: Endpoint[String, UUID, ApiError, WaitlistEntryResponse, Any] =
    BaseEndpoint.securedEndpoint.post
      .in("waitlist" / path[UUID]("waitlistEntryId") / "accept")
      .out(jsonBody[WaitlistEntryResponse])
      .tag("Waitlist")

  val getWaitlist: Endpoint[Unit, UUID, ApiError, List[WaitlistEntryResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("class-instances" / path[UUID]("classInstanceId") / "waitlist")
      .out(jsonBody[List[WaitlistEntryResponse]])
      .tag("Waitlist")

  // --- Cancellation Policies ---

  val createCancellationPolicy: Endpoint[Unit, CreateCancellationPolicyRequest, ApiError, CancellationPolicyResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("cancellation-policies")
      .in(jsonBody[CreateCancellationPolicyRequest])
      .out(jsonBody[CancellationPolicyResponse])
      .tag("Cancellation Policies")

  val listCancellationPolicies: Endpoint[Unit, Unit, ApiError, List[CancellationPolicyResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("cancellation-policies")
      .out(jsonBody[List[CancellationPolicyResponse]])
      .tag("Cancellation Policies")
