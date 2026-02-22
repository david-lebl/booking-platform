package com.bookingplatform.api.endpoints

import com.bookingplatform.api.codecs.TapirCodecs.given
import com.bookingplatform.api.errors.ApiError
import com.bookingplatform.shared.models.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import java.util.UUID

object SchedulingEndpoints:

  val createClassDefinition: Endpoint[Unit, CreateClassDefinitionRequest, ApiError, ClassDefinitionResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("class-definitions")
      .in(jsonBody[CreateClassDefinitionRequest])
      .out(jsonBody[ClassDefinitionResponse])
      .tag("Scheduling")

  val getClassDefinition: Endpoint[Unit, UUID, ApiError, ClassDefinitionResponse, Any] =
    BaseEndpoint.baseEndpoint.get
      .in("class-definitions" / path[UUID]("classDefinitionId"))
      .out(jsonBody[ClassDefinitionResponse])
      .tag("Scheduling")

  val listClassDefinitions: Endpoint[Unit, Unit, ApiError, List[ClassDefinitionResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("class-definitions")
      .out(jsonBody[List[ClassDefinitionResponse]])
      .tag("Scheduling")

  val createWeeklySchedule: Endpoint[Unit, CreateWeeklyScheduleRequest, ApiError, WeeklyScheduleResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("weekly-schedules")
      .in(jsonBody[CreateWeeklyScheduleRequest])
      .out(jsonBody[WeeklyScheduleResponse])
      .tag("Scheduling")

  val generateInstances: Endpoint[Unit, GenerateInstancesRequest, ApiError, List[ClassInstanceResponse], Any] =
    BaseEndpoint.baseEndpoint.post
      .in("class-instances" / "generate")
      .in(jsonBody[GenerateInstancesRequest])
      .out(jsonBody[List[ClassInstanceResponse]])
      .tag("Scheduling")

  val listInstances: Endpoint[Unit, (String, String), ApiError, List[ClassInstanceResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("class-instances")
      .in(query[String]("from"))
      .in(query[String]("to"))
      .out(jsonBody[List[ClassInstanceResponse]])
      .tag("Scheduling")
