package com.booking.application

import com.booking.domain.model.*
import com.booking.domain.repository.*
import zio.*
import java.time.Instant

case class CreateClassTypeCommand(
  name: String,
  description: String,
  durationMinutes: Int,
  difficultyLevel: DifficultyLevel,
  maxCapacity: Int,
  priceInCents: Long,
)

case class ScheduleClassSessionCommand(
  classTypeId: ClassTypeId,
  instructorId: InstructorId,
  studioId: StudioId,
  startTime: Instant,
  maxCapacity: Int,
)

case class ClassApplicationService(
  classTypeRepo: ClassTypeRepository,
  sessionRepo: ClassSessionRepository,
  instructorRepo: InstructorRepository,
  studioRepo: StudioRepository,
):

  def createClassType(cmd: CreateClassTypeCommand): Task[ClassType] =
    val ct = ClassType.create(
      name = cmd.name,
      description = cmd.description,
      durationMinutes = cmd.durationMinutes,
      difficultyLevel = cmd.difficultyLevel,
      maxCapacity = cmd.maxCapacity,
      priceInCents = cmd.priceInCents,
    )
    classTypeRepo.save(ct)

  def listClassTypes: Task[List[ClassType]] =
    classTypeRepo.findAll

  def getClassType(id: ClassTypeId): Task[Option[ClassType]] =
    classTypeRepo.findById(id)

  def scheduleSession(cmd: ScheduleClassSessionCommand): Task[ClassSession] =
    for
      _ <- instructorRepo.findById(cmd.instructorId).flatMap {
             case None    => ZIO.fail(new NoSuchElementException(s"Instructor not found: ${cmd.instructorId}"))
             case Some(_) => ZIO.unit
           }
      _ <- studioRepo.findById(cmd.studioId).flatMap {
             case None    => ZIO.fail(new NoSuchElementException(s"Studio not found: ${cmd.studioId}"))
             case Some(_) => ZIO.unit
           }
      session = ClassSession.schedule(
                  classTypeId  = cmd.classTypeId,
                  instructorId = cmd.instructorId,
                  studioId     = cmd.studioId,
                  startTime    = cmd.startTime,
                  maxCapacity  = cmd.maxCapacity,
                )
      saved <- sessionRepo.save(session)
    yield saved

  def listUpcomingSessions: Task[List[ClassSession]] =
    sessionRepo.findByDateRange(Instant.now(), Instant.now().plusSeconds(7 * 24 * 3600))

  def getSession(id: ClassSessionId): Task[Option[ClassSession]] =
    sessionRepo.findById(id)

  def listInstructors: Task[List[Instructor]] =
    instructorRepo.findAll

  def listStudios: Task[List[Studio]] =
    studioRepo.findAll

object ClassApplicationService:
  val layer: ZLayer[ClassTypeRepository & ClassSessionRepository & InstructorRepository & StudioRepository, Nothing, ClassApplicationService] =
    ZLayer.fromFunction(ClassApplicationService.apply)
