package com.bookingplatform.infrastructure.db.repositories

import com.bookingplatform.core.common.*
import com.bookingplatform.core.scheduling.*
import com.bookingplatform.shared.ids.*
import zio.*

import java.time.Instant

final case class InMemoryClassDefinitionRepository(ref: Ref[Map[ClassDefinitionId, ClassDefinition]])
    extends ClassDefinitionRepository:
  def create(cd: ClassDefinition): IO[DomainError, ClassDefinition] =
    ref.update(_ + (cd.id -> cd)).as(cd)

  def findById(id: ClassDefinitionId): IO[DomainError, Option[ClassDefinition]] =
    ref.get.map(_.get(id))

  def findByVenue(venueId: VenueId): IO[DomainError, List[ClassDefinition]] =
    ref.get.map(_.values.filter(_.venueId == venueId).toList)

  def findByInstructor(instructorId: UserId): IO[DomainError, List[ClassDefinition]] =
    ref.get.map(_.values.filter(_.instructorId == instructorId).toList)

  def findAll: IO[DomainError, List[ClassDefinition]] =
    ref.get.map(_.values.toList)

object InMemoryClassDefinitionRepository:
  val layer: ULayer[ClassDefinitionRepository] = ZLayer {
    Ref.make(Map.empty[ClassDefinitionId, ClassDefinition]).map(InMemoryClassDefinitionRepository(_))
  }

final case class InMemoryClassInstanceRepository(
    ref: Ref[Map[ClassInstanceId, ClassInstance]],
    classDefRepo: ClassDefinitionRepository
) extends ClassInstanceRepository:
  def create(ci: ClassInstance): IO[DomainError, ClassInstance] =
    ref.update(_ + (ci.id -> ci)).as(ci)

  def createBatch(instances: List[ClassInstance]): IO[DomainError, List[ClassInstance]] =
    ref.update(map => instances.foldLeft(map)((m, ci) => m + (ci.id -> ci))).as(instances)

  def findById(id: ClassInstanceId): IO[DomainError, Option[ClassInstance]] =
    ref.get.map(_.get(id))

  def findByClassDefinition(classDefId: ClassDefinitionId): IO[DomainError, List[ClassInstance]] =
    ref.get.map(_.values.filter(_.classDefinitionId == classDefId).toList)

  def findByDateRange(from: Instant, to: Instant): IO[DomainError, List[ClassInstance]] =
    ref.get.map(_.values.filter(ci =>
      !ci.startTime.isBefore(from) && ci.startTime.isBefore(to)
    ).toList.sortBy(_.startTime))

  def findByVenueAndDateRange(venueId: VenueId, from: Instant, to: Instant): IO[DomainError, List[ClassInstance]] =
    for
      classDefs <- classDefRepo.findByVenue(venueId)
      classDefIds = classDefs.map(_.id).toSet
      instances <- ref.get.map(_.values.filter(ci =>
        classDefIds.contains(ci.classDefinitionId) &&
          !ci.startTime.isBefore(from) && ci.startTime.isBefore(to)
      ).toList.sortBy(_.startTime))
    yield instances

  def update(ci: ClassInstance): IO[DomainError, ClassInstance] =
    ref.update(_ + (ci.id -> ci)).as(ci)

object InMemoryClassInstanceRepository:
  val layer: URLayer[ClassDefinitionRepository, ClassInstanceRepository] = ZLayer {
    for
      ref      <- Ref.make(Map.empty[ClassInstanceId, ClassInstance])
      classDefRepo <- ZIO.service[ClassDefinitionRepository]
    yield InMemoryClassInstanceRepository(ref, classDefRepo)
  }

final case class InMemoryWeeklyScheduleRepository(ref: Ref[Map[WeeklyScheduleId, WeeklySchedule]])
    extends WeeklyScheduleRepository:
  def create(ws: WeeklySchedule): IO[DomainError, WeeklySchedule] =
    ref.update(_ + (ws.id -> ws)).as(ws)

  def findByClassDefinition(classDefId: ClassDefinitionId): IO[DomainError, List[WeeklySchedule]] =
    ref.get.map(_.values.filter(_.classDefinitionId == classDefId).toList)

  def delete(id: WeeklyScheduleId): IO[DomainError, Unit] =
    ref.update(_ - id)

object InMemoryWeeklyScheduleRepository:
  val layer: ULayer[WeeklyScheduleRepository] = ZLayer {
    Ref.make(Map.empty[WeeklyScheduleId, WeeklySchedule]).map(InMemoryWeeklyScheduleRepository(_))
  }
