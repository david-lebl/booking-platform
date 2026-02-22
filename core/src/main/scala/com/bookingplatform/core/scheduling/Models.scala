package com.bookingplatform.core.scheduling

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*

import java.time.{DayOfWeek, Instant, LocalDate, LocalTime}

final case class BookingWindow(
    minAdvanceMinutes: Int,
    maxAdvanceDays: Int
)

final case class ClassDefinition(
    id: ClassDefinitionId,
    serviceDefinitionId: ServiceDefinitionId,
    venueId: VenueId,
    roomId: RoomId,
    instructorId: UserId,
    name: NonEmptyString,
    capacity: PositiveInt,
    durationMinutes: PositiveInt,
    bookingWindow: BookingWindow,
    cancellationPolicyId: Option[CancellationPolicyId],
    createdAt: Instant
)

final case class ClassInstance(
    id: ClassInstanceId,
    classDefinitionId: ClassDefinitionId,
    startTime: Instant,
    endTime: Instant,
    currentBookings: Int,
    capacity: Int,
    status: ClassInstanceStatus,
    createdAt: Instant
):
  def isFull: Boolean           = currentBookings >= capacity
  def hasAvailableSpots: Boolean = currentBookings < capacity
  def availableSpots: Int       = capacity - currentBookings

final case class WeeklySchedule(
    id: WeeklyScheduleId,
    classDefinitionId: ClassDefinitionId,
    dayOfWeek: DayOfWeek,
    startTime: LocalTime,
    effectiveFrom: LocalDate,
    effectiveUntil: Option[LocalDate]
)

final case class InstructorAvailability(
    instructorId: UserId,
    dayOfWeek: DayOfWeek,
    startTime: LocalTime,
    endTime: LocalTime
)
