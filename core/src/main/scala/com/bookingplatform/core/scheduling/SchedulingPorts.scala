package com.bookingplatform.core.scheduling

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import zio.*

import java.time.{Instant, LocalDate}

trait ClassDefinitionRepository:
  def create(classDef: ClassDefinition): IO[DomainError, ClassDefinition]
  def findById(id: ClassDefinitionId): IO[DomainError, Option[ClassDefinition]]
  def findByVenue(venueId: VenueId): IO[DomainError, List[ClassDefinition]]
  def findByInstructor(instructorId: UserId): IO[DomainError, List[ClassDefinition]]
  def findAll: IO[DomainError, List[ClassDefinition]]

trait ClassInstanceRepository:
  def create(instance: ClassInstance): IO[DomainError, ClassInstance]
  def createBatch(instances: List[ClassInstance]): IO[DomainError, List[ClassInstance]]
  def findById(id: ClassInstanceId): IO[DomainError, Option[ClassInstance]]
  def findByClassDefinition(classDefId: ClassDefinitionId): IO[DomainError, List[ClassInstance]]
  def findByDateRange(from: Instant, to: Instant): IO[DomainError, List[ClassInstance]]
  def findByVenueAndDateRange(venueId: VenueId, from: Instant, to: Instant): IO[DomainError, List[ClassInstance]]
  def update(instance: ClassInstance): IO[DomainError, ClassInstance]

trait WeeklyScheduleRepository:
  def create(schedule: WeeklySchedule): IO[DomainError, WeeklySchedule]
  def findByClassDefinition(classDefId: ClassDefinitionId): IO[DomainError, List[WeeklySchedule]]
  def delete(id: WeeklyScheduleId): IO[DomainError, Unit]
