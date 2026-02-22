package com.booking.presentation.dto

import com.booking.domain.model.*
import sttp.tapir.generic.auto.*
import zio.json.*
import java.time.Instant
import java.util.UUID

// --- Member DTOs ---
case class RegisterMemberRequest(
  firstName: String,
  lastName: String,
  email: String,
  phone: Option[String],
  membershipType: String,
) derives JsonDecoder, JsonEncoder

case class MemberResponse(
  id: String,
  firstName: String,
  lastName: String,
  email: String,
  phone: Option[String],
  membershipType: String,
  active: Boolean,
  createdAt: String,
) derives JsonDecoder, JsonEncoder

object MemberResponse:
  def from(member: Member): MemberResponse =
    MemberResponse(
      id            = member.id.value.toString,
      firstName     = member.name.first,
      lastName      = member.name.last,
      email         = member.email.value,
      phone         = member.phone.map(_.value),
      membershipType = member.membershipType.toString,
      active        = member.active,
      createdAt     = member.createdAt.toString,
    )

// --- Instructor DTOs ---
case class CreateInstructorRequest(
  firstName: String,
  lastName: String,
  email: String,
  phone: Option[String],
  bio: Option[String],
  specializations: List[String],
) derives JsonDecoder, JsonEncoder

case class InstructorResponse(
  id: String,
  firstName: String,
  lastName: String,
  email: String,
  specializations: List[String],
  active: Boolean,
) derives JsonDecoder, JsonEncoder

object InstructorResponse:
  def from(instructor: Instructor): InstructorResponse =
    InstructorResponse(
      id              = instructor.id.value.toString,
      firstName       = instructor.name.first,
      lastName        = instructor.name.last,
      email           = instructor.email.value,
      specializations = instructor.specializations,
      active          = instructor.active,
    )

// --- ClassType DTOs ---
case class CreateClassTypeRequest(
  name: String,
  description: String,
  durationMinutes: Int,
  difficultyLevel: String,
  maxCapacity: Int,
  priceInCents: Long,
) derives JsonDecoder, JsonEncoder

case class ClassTypeResponse(
  id: String,
  name: String,
  description: String,
  durationMinutes: Int,
  difficultyLevel: String,
  maxCapacity: Int,
  priceInCents: Long,
) derives JsonDecoder, JsonEncoder

object ClassTypeResponse:
  def from(ct: ClassType): ClassTypeResponse =
    ClassTypeResponse(
      id              = ct.id.value.toString,
      name            = ct.name,
      description     = ct.description,
      durationMinutes = ct.durationMinutes,
      difficultyLevel = ct.difficultyLevel.toString,
      maxCapacity     = ct.maxCapacity,
      priceInCents    = ct.priceInCents,
    )

// --- ClassSession DTOs ---
case class ScheduleSessionRequest(
  classTypeId: String,
  instructorId: String,
  studioId: String,
  startTime: String,
  maxCapacity: Int,
) derives JsonDecoder, JsonEncoder

case class ClassSessionResponse(
  id: String,
  classTypeId: String,
  instructorId: String,
  studioId: String,
  startTime: String,
  maxCapacity: Int,
  bookedCount: Int,
  availableSpots: Int,
  status: String,
) derives JsonDecoder, JsonEncoder

object ClassSessionResponse:
  def from(session: ClassSession): ClassSessionResponse =
    ClassSessionResponse(
      id             = session.id.value.toString,
      classTypeId    = session.classTypeId.value.toString,
      instructorId   = session.instructorId.value.toString,
      studioId       = session.studioId.value.toString,
      startTime      = session.startTime.toString,
      maxCapacity    = session.maxCapacity,
      bookedCount    = session.bookedCount,
      availableSpots = session.availableSpots,
      status         = session.status.toString,
    )

// --- Booking DTOs ---
case class CreateBookingRequest(
  memberId: String,
  classSessionId: String,
) derives JsonDecoder, JsonEncoder

case class BookingResponse(
  id: String,
  memberId: String,
  classSessionId: String,
  status: String,
  createdAt: String,
  cancelledAt: Option[String],
) derives JsonDecoder, JsonEncoder

object BookingResponse:
  def from(booking: Booking): BookingResponse =
    BookingResponse(
      id             = booking.id.value.toString,
      memberId       = booking.memberId.value.toString,
      classSessionId = booking.classSessionId.value.toString,
      status         = booking.status.toString,
      createdAt      = booking.createdAt.toString,
      cancelledAt    = booking.cancelledAt.map(_.toString),
    )

// --- Error DTO ---
case class ErrorResponse(message: String) derives JsonDecoder, JsonEncoder

// --- Studio DTOs ---
case class CreateStudioRequest(
  name: String,
  street: String,
  city: String,
  country: String,
  postalCode: String,
  capacity: Int,
) derives JsonDecoder, JsonEncoder

case class StudioResponse(
  id: String,
  name: String,
  street: String,
  city: String,
  country: String,
  postalCode: String,
  capacity: Int,
  active: Boolean,
) derives JsonDecoder, JsonEncoder

object StudioResponse:
  def from(studio: Studio): StudioResponse =
    StudioResponse(
      id         = studio.id.value.toString,
      name       = studio.name,
      street     = studio.address.street,
      city       = studio.address.city,
      country    = studio.address.country,
      postalCode = studio.address.postalCode,
      capacity   = studio.capacity,
      active     = studio.active,
    )
