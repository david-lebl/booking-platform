package com.booking.presentation.api

import com.booking.application.*
import com.booking.domain.model.*
import com.booking.presentation.dto.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import zio.*
import java.time.Instant
import java.util.UUID

class ClassApi(service: ClassApplicationService):

  private val base = endpoint.errorOut(jsonBody[ErrorResponse])

  val createClassTypeEndpoint =
    base.post
      .in("api" / "class-types")
      .in(jsonBody[CreateClassTypeRequest])
      .out(jsonBody[ClassTypeResponse])
      .zServerLogic { req =>
        val difficulty = req.difficultyLevel match
          case "Intermediate" => DifficultyLevel.Intermediate
          case "Advanced"     => DifficultyLevel.Advanced
          case "AllLevels"    => DifficultyLevel.AllLevels
          case _              => DifficultyLevel.Beginner
        service.createClassType(CreateClassTypeCommand(
          name            = req.name,
          description     = req.description,
          durationMinutes = req.durationMinutes,
          difficultyLevel = difficulty,
          maxCapacity     = req.maxCapacity,
          priceInCents    = req.priceInCents,
        )).map(ClassTypeResponse.from).mapError(e => ErrorResponse(e.getMessage))
      }

  val listClassTypesEndpoint =
    base.get
      .in("api" / "class-types")
      .out(jsonBody[List[ClassTypeResponse]])
      .zServerLogic { _ =>
        service.listClassTypes.map(_.map(ClassTypeResponse.from)).mapError(e => ErrorResponse(e.getMessage))
      }

  val scheduleSessionEndpoint =
    base.post
      .in("api" / "sessions")
      .in(jsonBody[ScheduleSessionRequest])
      .out(jsonBody[ClassSessionResponse])
      .zServerLogic { req =>
        ZIO.attempt {
          ScheduleClassSessionCommand(
            classTypeId  = ClassTypeId(UUID.fromString(req.classTypeId)),
            instructorId = InstructorId(UUID.fromString(req.instructorId)),
            studioId     = StudioId(UUID.fromString(req.studioId)),
            startTime    = Instant.parse(req.startTime),
            maxCapacity  = req.maxCapacity,
          )
        }
        .flatMap(service.scheduleSession)
        .map(ClassSessionResponse.from)
        .mapError(e => ErrorResponse(e.getMessage))
      }

  val listSessionsEndpoint =
    base.get
      .in("api" / "sessions")
      .out(jsonBody[List[ClassSessionResponse]])
      .zServerLogic { _ =>
        service.listUpcomingSessions.map(_.map(ClassSessionResponse.from)).mapError(e => ErrorResponse(e.getMessage))
      }

  val getSessionEndpoint =
    base.get
      .in("api" / "sessions" / path[String]("id"))
      .out(jsonBody[ClassSessionResponse])
      .zServerLogic { idStr =>
        ZIO.attempt(UUID.fromString(idStr))
          .flatMap(uuid => service.getSession(ClassSessionId(uuid)))
          .flatMap {
            case Some(s) => ZIO.succeed(ClassSessionResponse.from(s))
            case None    => ZIO.fail(new NoSuchElementException(s"Session not found: $idStr"))
          }
          .mapError(e => ErrorResponse(e.getMessage))
      }

  val listInstructorsEndpoint =
    base.get
      .in("api" / "instructors")
      .out(jsonBody[List[InstructorResponse]])
      .zServerLogic { _ =>
        service.listInstructors.map(_.map(InstructorResponse.from)).mapError(e => ErrorResponse(e.getMessage))
      }

  val listStudiosEndpoint =
    base.get
      .in("api" / "studios")
      .out(jsonBody[List[StudioResponse]])
      .zServerLogic { _ =>
        service.listStudios.map(_.map(StudioResponse.from)).mapError(e => ErrorResponse(e.getMessage))
      }

  val routes = List(
    createClassTypeEndpoint,
    listClassTypesEndpoint,
    scheduleSessionEndpoint,
    listSessionsEndpoint,
    getSessionEndpoint,
    listInstructorsEndpoint,
    listStudiosEndpoint,
  )

object ClassApi:
  val layer: ZLayer[ClassApplicationService, Nothing, ClassApi] =
    ZLayer.fromFunction(ClassApi.apply)
