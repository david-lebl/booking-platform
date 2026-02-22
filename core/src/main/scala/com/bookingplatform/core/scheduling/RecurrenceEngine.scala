package com.bookingplatform.core.scheduling

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.ClassInstanceStatus

import java.time.{Instant, LocalDate, LocalTime, ZoneId, ZonedDateTime}

object RecurrenceEngine:

  def generateInstances(
      classDef: ClassDefinition,
      schedules: List[WeeklySchedule],
      fromDate: LocalDate,
      toDate: LocalDate,
      timezone: ZoneId
  ): List[ClassInstance] =
    for
      schedule <- schedules
      if schedule.effectiveFrom.isBefore(toDate) || schedule.effectiveFrom.isEqual(toDate)
      if schedule.effectiveUntil.forall(eu => eu.isAfter(fromDate) || eu.isEqual(fromDate))
      date <- dateRange(
        maxDate(fromDate, schedule.effectiveFrom),
        minDate(toDate, schedule.effectiveUntil.getOrElse(toDate))
      )
      if date.getDayOfWeek == schedule.dayOfWeek
    yield
      val startZoned = ZonedDateTime.of(date, schedule.startTime, timezone)
      val endZoned   = startZoned.plusMinutes(PositiveInt.unwrap(classDef.durationMinutes).toLong)
      ClassInstance(
        id = ClassInstanceId.generate,
        classDefinitionId = classDef.id,
        startTime = startZoned.toInstant,
        endTime = endZoned.toInstant,
        currentBookings = 0,
        capacity = PositiveInt.unwrap(classDef.capacity),
        status = ClassInstanceStatus.Scheduled,
        createdAt = Instant.now()
      )

  private def dateRange(from: LocalDate, to: LocalDate): LazyList[LocalDate] =
    LazyList.iterate(from)(_.plusDays(1)).takeWhile(d => !d.isAfter(to))

  private def maxDate(a: LocalDate, b: LocalDate): LocalDate =
    if a.isAfter(b) then a else b

  private def minDate(a: LocalDate, b: LocalDate): LocalDate =
    if a.isBefore(b) then a else b
