package com.bookingplatform.core.scheduling

import com.bookingplatform.core.common.*
import com.bookingplatform.core.catalog.{CatalogService, Venue}
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.ClassInstanceStatus
import zio.*

import java.time.{DayOfWeek, Instant, LocalDate, LocalTime, ZoneId}

final case class SchedulingService(
    classDefRepo: ClassDefinitionRepository,
    classInstanceRepo: ClassInstanceRepository,
    weeklyScheduleRepo: WeeklyScheduleRepository,
    catalogService: CatalogService
):

  def createClassDefinition(
      serviceDefinitionId: ServiceDefinitionId,
      venueId: VenueId,
      roomId: RoomId,
      instructorId: UserId,
      name: NonEmptyString,
      capacity: PositiveInt,
      durationMinutes: PositiveInt,
      bookingWindowMinAdvanceMinutes: Int,
      bookingWindowMaxAdvanceDays: Int,
      cancellationPolicyId: Option[CancellationPolicyId]
  ): IO[DomainError, ClassDefinition] =
    for
      _ <- catalogService.getVenue(venueId)
      classDef = ClassDefinition(
        id = ClassDefinitionId.generate,
        serviceDefinitionId = serviceDefinitionId,
        venueId = venueId,
        roomId = roomId,
        instructorId = instructorId,
        name = name,
        capacity = capacity,
        durationMinutes = durationMinutes,
        bookingWindow = BookingWindow(bookingWindowMinAdvanceMinutes, bookingWindowMaxAdvanceDays),
        cancellationPolicyId = cancellationPolicyId,
        createdAt = Instant.now()
      )
      created <- classDefRepo.create(classDef)
    yield created

  def getClassDefinition(id: ClassDefinitionId): IO[DomainError, ClassDefinition] =
    classDefRepo.findById(id).flatMap {
      case Some(cd) => ZIO.succeed(cd)
      case None     => ZIO.fail(DomainError.notFound("ClassDefinition", id.value))
    }

  def listClassDefinitions: IO[DomainError, List[ClassDefinition]] =
    classDefRepo.findAll

  def createWeeklySchedule(
      classDefinitionId: ClassDefinitionId,
      dayOfWeek: DayOfWeek,
      startTime: LocalTime,
      effectiveFrom: LocalDate,
      effectiveUntil: Option[LocalDate]
  ): IO[DomainError, WeeklySchedule] =
    for
      _ <- getClassDefinition(classDefinitionId)
      schedule = WeeklySchedule(
        id = WeeklyScheduleId.generate,
        classDefinitionId = classDefinitionId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        effectiveFrom = effectiveFrom,
        effectiveUntil = effectiveUntil
      )
      created <- weeklyScheduleRepo.create(schedule)
    yield created

  def generateInstances(
      classDefinitionId: ClassDefinitionId,
      fromDate: LocalDate,
      toDate: LocalDate
  ): IO[DomainError, List[ClassInstance]] =
    for
      classDef  <- getClassDefinition(classDefinitionId)
      venue     <- catalogService.getVenue(classDef.venueId)
      schedules <- weeklyScheduleRepo.findByClassDefinition(classDefinitionId)
      timezone   = ZoneId.of(Timezone.unwrap(venue.timezone))
      instances  = RecurrenceEngine.generateInstances(classDef, schedules, fromDate, toDate, timezone)
      created   <- classInstanceRepo.createBatch(instances)
    yield created

  def getClassInstance(id: ClassInstanceId): IO[DomainError, ClassInstance] =
    classInstanceRepo.findById(id).flatMap {
      case Some(ci) => ZIO.succeed(ci)
      case None     => ZIO.fail(DomainError.notFound("ClassInstance", id.value))
    }

  def listInstancesByDateRange(from: Instant, to: Instant): IO[DomainError, List[ClassInstance]] =
    classInstanceRepo.findByDateRange(from, to)

  def listInstancesByVenue(venueId: VenueId, from: Instant, to: Instant): IO[DomainError, List[ClassInstance]] =
    classInstanceRepo.findByVenueAndDateRange(venueId, from, to)

  def isWithinBookingWindow(classDef: ClassDefinition, classInstance: ClassInstance, now: Instant): Boolean =
    val minAdvance = now.plusSeconds(classDef.bookingWindow.minAdvanceMinutes * 60L)
    val maxAdvance = now.plus(java.time.Duration.ofDays(classDef.bookingWindow.maxAdvanceDays.toLong))
    classInstance.startTime.isAfter(minAdvance) && classInstance.startTime.isBefore(maxAdvance)

object SchedulingService:
  val layer: URLayer[
    ClassDefinitionRepository & ClassInstanceRepository & WeeklyScheduleRepository & CatalogService,
    SchedulingService
  ] = ZLayer.fromFunction(SchedulingService.apply)
