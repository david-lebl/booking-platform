package com.bookingplatform.infrastructure.restapi.controllers

import com.bookingplatform.api.endpoints.SchedulingEndpoints
import com.bookingplatform.api.errors.{ApiError, ErrorMapping}
import com.bookingplatform.core.common.*
import com.bookingplatform.core.scheduling.{ClassDefinition, ClassInstance, SchedulingService, WeeklySchedule}
import com.bookingplatform.infrastructure.restapi.controllers.ValidationHelper.validate
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*
import sttp.tapir.ztapir.*
import zio.*

import java.time.{DayOfWeek, Instant, LocalDate, LocalTime}

object SchedulingController:

  def endpoints(schedulingService: SchedulingService) = List(
    createClassDefinition(schedulingService),
    getClassDefinition(schedulingService),
    listClassDefinitions(schedulingService),
    createWeeklySchedule(schedulingService),
    generateInstances(schedulingService),
    listInstances(schedulingService)
  )

  private def toClassDefResponse(cd: ClassDefinition): ClassDefinitionResponse =
    ClassDefinitionResponse(
      cd.id.value, cd.serviceDefinitionId.value, cd.venueId.value, cd.roomId.value,
      cd.instructorId.value, NonEmptyString.unwrap(cd.name), PositiveInt.unwrap(cd.capacity),
      PositiveInt.unwrap(cd.durationMinutes), cd.bookingWindow.minAdvanceMinutes,
      cd.bookingWindow.maxAdvanceDays, cd.cancellationPolicyId.map(_.value), cd.createdAt
    )

  private def toInstanceResponse(ci: ClassInstance): ClassInstanceResponse =
    ClassInstanceResponse(ci.id.value, ci.classDefinitionId.value, ci.startTime, ci.endTime,
      ci.currentBookings, ci.capacity, ci.status)

  private def createClassDefinition(svc: SchedulingService) =
    SchedulingEndpoints.createClassDefinition.zServerLogic { req =>
      (for
        name     <- validate(NonEmptyString.make(req.name))
        capacity <- validate(PositiveInt.make(req.capacity))
        duration <- validate(PositiveInt.make(req.durationMinutes))
        cd <- svc.createClassDefinition(
          ServiceDefinitionId(req.serviceDefinitionId), VenueId(req.venueId), RoomId(req.roomId),
          UserId(req.instructorId), name, capacity, duration, req.bookingWindowMinAdvanceMinutes,
          req.bookingWindowMaxAdvanceDays, req.cancellationPolicyId.map(CancellationPolicyId(_))
        )
      yield toClassDefResponse(cd)).mapError(ErrorMapping.toApiError)
    }

  private def getClassDefinition(svc: SchedulingService) =
    SchedulingEndpoints.getClassDefinition.zServerLogic { id =>
      svc.getClassDefinition(ClassDefinitionId(id)).map(toClassDefResponse).mapError(ErrorMapping.toApiError)
    }

  private def listClassDefinitions(svc: SchedulingService) =
    SchedulingEndpoints.listClassDefinitions.zServerLogic { _ =>
      svc.listClassDefinitions.map(_.map(toClassDefResponse)).mapError(ErrorMapping.toApiError)
    }

  private def createWeeklySchedule(svc: SchedulingService) =
    SchedulingEndpoints.createWeeklySchedule.zServerLogic { req =>
      (for
        dayOfWeek <- ZIO.attempt(DayOfWeek.valueOf(req.dayOfWeek.toUpperCase))
          .mapError(_ => DomainError.ValidationError(s"Invalid day of week: ${req.dayOfWeek}"))
        startTime <- ZIO.attempt(LocalTime.parse(req.startTime))
          .mapError(_ => DomainError.ValidationError(s"Invalid time format: ${req.startTime}"))
        ws <- svc.createWeeklySchedule(
          ClassDefinitionId(req.classDefinitionId), dayOfWeek, startTime,
          req.effectiveFrom, req.effectiveUntil
        )
      yield WeeklyScheduleResponse(ws.id.value, ws.classDefinitionId.value,
        ws.dayOfWeek.toString, ws.startTime.toString, ws.effectiveFrom, ws.effectiveUntil)
      ).mapError(ErrorMapping.toApiError)
    }

  private def generateInstances(svc: SchedulingService) =
    SchedulingEndpoints.generateInstances.zServerLogic { req =>
      svc.generateInstances(ClassDefinitionId(req.classDefinitionId), req.fromDate, req.toDate)
        .map(_.map(toInstanceResponse))
        .mapError(ErrorMapping.toApiError)
    }

  private def listInstances(svc: SchedulingService) =
    SchedulingEndpoints.listInstances.zServerLogic { case (from, to) =>
      (for
        fromInstant <- ZIO.attempt(Instant.parse(from)).mapError(_ => DomainError.ValidationError(s"Invalid from date: $from"))
        toInstant   <- ZIO.attempt(Instant.parse(to)).mapError(_ => DomainError.ValidationError(s"Invalid to date: $to"))
        instances   <- svc.listInstancesByDateRange(fromInstant, toInstant)
      yield instances.map(toInstanceResponse)).mapError(ErrorMapping.toApiError)
    }
