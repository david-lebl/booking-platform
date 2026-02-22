package com.bookingplatform.core.scheduling

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import zio.test.*
import zio.test.Assertion.*

import java.time.{DayOfWeek, LocalDate, LocalTime, ZoneId}

object RecurrenceEngineSpec extends ZIOSpecDefault:

  private def makeClassDef(capacity: Int = 10, durationMinutes: Int = 60) =
    ClassDefinition(
      id = ClassDefinitionId.generate,
      serviceDefinitionId = ServiceDefinitionId.generate,
      venueId = VenueId.generate,
      roomId = RoomId.generate,
      instructorId = UserId.generate,
      name = NonEmptyString.wrap("Test Class"),
      capacity = PositiveInt.wrap(capacity),
      durationMinutes = PositiveInt.wrap(durationMinutes),
      bookingWindow = BookingWindow(0, 30),
      cancellationPolicyId = None,
      createdAt = java.time.Instant.now()
    )

  def spec = suite("RecurrenceEngine")(
    test("generates instances for matching days") {
      val classDef = makeClassDef()
      val schedule = WeeklySchedule(
        id = WeeklyScheduleId.generate,
        classDefinitionId = classDef.id,
        dayOfWeek = DayOfWeek.MONDAY,
        startTime = LocalTime.of(9, 0),
        effectiveFrom = LocalDate.of(2026, 1, 1),
        effectiveUntil = None
      )
      val instances = RecurrenceEngine.generateInstances(
        classDef, List(schedule),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
        ZoneId.of("Europe/Prague")
      )
      // January 2026 has Mondays on 5, 12, 19, 26
      assertTrue(instances.length == 4)
    },
    test("respects effectiveUntil date") {
      val classDef = makeClassDef()
      val schedule = WeeklySchedule(
        id = WeeklyScheduleId.generate,
        classDefinitionId = classDef.id,
        dayOfWeek = DayOfWeek.MONDAY,
        startTime = LocalTime.of(9, 0),
        effectiveFrom = LocalDate.of(2026, 1, 1),
        effectiveUntil = Some(LocalDate.of(2026, 1, 15))
      )
      val instances = RecurrenceEngine.generateInstances(
        classDef, List(schedule),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
        ZoneId.of("Europe/Prague")
      )
      // Only Mondays up to Jan 15: Jan 5, Jan 12
      assertTrue(instances.length == 2)
    },
    test("generates no instances for empty date range") {
      val classDef = makeClassDef()
      val schedule = WeeklySchedule(
        id = WeeklyScheduleId.generate,
        classDefinitionId = classDef.id,
        dayOfWeek = DayOfWeek.MONDAY,
        startTime = LocalTime.of(9, 0),
        effectiveFrom = LocalDate.of(2026, 2, 1),
        effectiveUntil = None
      )
      val instances = RecurrenceEngine.generateInstances(
        classDef, List(schedule),
        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10),
        ZoneId.of("Europe/Prague")
      )
      assertTrue(instances.isEmpty)
    },
    test("generates instances for multiple schedules") {
      val classDef = makeClassDef()
      val mondaySchedule = WeeklySchedule(
        id = WeeklyScheduleId.generate,
        classDefinitionId = classDef.id,
        dayOfWeek = DayOfWeek.MONDAY,
        startTime = LocalTime.of(9, 0),
        effectiveFrom = LocalDate.of(2026, 1, 1),
        effectiveUntil = None
      )
      val wednesdaySchedule = WeeklySchedule(
        id = WeeklyScheduleId.generate,
        classDefinitionId = classDef.id,
        dayOfWeek = DayOfWeek.WEDNESDAY,
        startTime = LocalTime.of(10, 0),
        effectiveFrom = LocalDate.of(2026, 1, 1),
        effectiveUntil = None
      )
      val instances = RecurrenceEngine.generateInstances(
        classDef, List(mondaySchedule, wednesdaySchedule),
        LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 11),
        ZoneId.of("Europe/Prague")
      )
      // Jan 5 (Mon) + Jan 7 (Wed) = 2
      assertTrue(instances.length == 2)
    },
    test("sets correct capacity on generated instances") {
      val classDef = makeClassDef(capacity = 15)
      val schedule = WeeklySchedule(
        id = WeeklyScheduleId.generate,
        classDefinitionId = classDef.id,
        dayOfWeek = DayOfWeek.MONDAY,
        startTime = LocalTime.of(9, 0),
        effectiveFrom = LocalDate.of(2026, 1, 1),
        effectiveUntil = None
      )
      val instances = RecurrenceEngine.generateInstances(
        classDef, List(schedule),
        LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 5),
        ZoneId.of("Europe/Prague")
      )
      assertTrue(instances.length == 1 && instances.head.capacity == 15 && instances.head.currentBookings == 0)
    }
  )
