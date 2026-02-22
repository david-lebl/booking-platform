package com.bookingplatform.api.endpoints

import com.bookingplatform.api.codecs.TapirCodecs.given
import com.bookingplatform.api.errors.ApiError
import com.bookingplatform.shared.models.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import java.util.UUID

object CatalogEndpoints:

  // --- Venues ---

  val createVenue: Endpoint[Unit, CreateVenueRequest, ApiError, VenueResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("venues")
      .in(jsonBody[CreateVenueRequest])
      .out(jsonBody[VenueResponse])
      .tag("Venues")

  val getVenue: Endpoint[Unit, UUID, ApiError, VenueResponse, Any] =
    BaseEndpoint.baseEndpoint.get
      .in("venues" / path[UUID]("venueId"))
      .out(jsonBody[VenueResponse])
      .tag("Venues")

  val listVenues: Endpoint[Unit, Unit, ApiError, List[VenueResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("venues")
      .out(jsonBody[List[VenueResponse]])
      .tag("Venues")

  val updateVenue: Endpoint[Unit, (UUID, UpdateVenueRequest), ApiError, VenueResponse, Any] =
    BaseEndpoint.baseEndpoint.put
      .in("venues" / path[UUID]("venueId"))
      .in(jsonBody[UpdateVenueRequest])
      .out(jsonBody[VenueResponse])
      .tag("Venues")

  // --- Rooms ---

  val createRoom: Endpoint[Unit, CreateRoomRequest, ApiError, RoomResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("rooms")
      .in(jsonBody[CreateRoomRequest])
      .out(jsonBody[RoomResponse])
      .tag("Rooms")

  val listRoomsByVenue: Endpoint[Unit, UUID, ApiError, List[RoomResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("venues" / path[UUID]("venueId") / "rooms")
      .out(jsonBody[List[RoomResponse]])
      .tag("Rooms")

  // --- Stations ---

  val createStation: Endpoint[Unit, CreateStationRequest, ApiError, StationResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("stations")
      .in(jsonBody[CreateStationRequest])
      .out(jsonBody[StationResponse])
      .tag("Stations")

  val listStationsByRoom: Endpoint[Unit, UUID, ApiError, List[StationResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("rooms" / path[UUID]("roomId") / "stations")
      .out(jsonBody[List[StationResponse]])
      .tag("Stations")

  // --- Service Definitions ---

  val createServiceDefinition: Endpoint[Unit, CreateServiceDefinitionRequest, ApiError, ServiceDefinitionResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("service-definitions")
      .in(jsonBody[CreateServiceDefinitionRequest])
      .out(jsonBody[ServiceDefinitionResponse])
      .tag("Service Definitions")

  val listServiceDefinitions: Endpoint[Unit, Unit, ApiError, List[ServiceDefinitionResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("service-definitions")
      .out(jsonBody[List[ServiceDefinitionResponse]])
      .tag("Service Definitions")
