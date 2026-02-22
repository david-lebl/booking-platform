package com.bookingplatform.core.catalog

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*
import zio.*

import java.time.Instant

final case class CatalogService(
    venueRepo: VenueRepository,
    roomRepo: RoomRepository,
    stationRepo: StationRepository,
    serviceDefRepo: ServiceDefinitionRepository
):

  // --- Venues ---

  def createVenue(name: NonEmptyString, address: NonEmptyString, timezone: Timezone): IO[DomainError, Venue] =
    val now = Instant.now()
    val venue = Venue(
      id = VenueId.generate,
      name = name,
      address = address,
      timezone = timezone,
      status = VenueStatus.Active,
      createdAt = now,
      updatedAt = now
    )
    venueRepo.create(venue)

  def getVenue(id: VenueId): IO[DomainError, Venue] =
    venueRepo.findById(id).flatMap {
      case Some(v) => ZIO.succeed(v)
      case None    => ZIO.fail(DomainError.notFound("Venue", id.value))
    }

  def listVenues: IO[DomainError, List[Venue]] = venueRepo.findAll

  def updateVenue(
      id: VenueId,
      name: Option[NonEmptyString],
      address: Option[NonEmptyString],
      timezone: Option[Timezone],
      status: Option[VenueStatus]
  ): IO[DomainError, Venue] =
    for
      venue <- getVenue(id)
      updated = venue.copy(
        name = name.getOrElse(venue.name),
        address = address.getOrElse(venue.address),
        timezone = timezone.getOrElse(venue.timezone),
        status = status.getOrElse(venue.status),
        updatedAt = Instant.now()
      )
      result <- venueRepo.update(updated)
    yield result

  // --- Rooms ---

  def createRoom(
      venueId: VenueId,
      name: NonEmptyString,
      capacity: PositiveInt,
      roomType: RoomType,
      stations: Int
  ): IO[DomainError, Room] =
    for
      _ <- getVenue(venueId)
      room = Room(
        id = RoomId.generate,
        venueId = venueId,
        name = name,
        capacity = capacity,
        roomType = roomType,
        stations = stations,
        createdAt = Instant.now()
      )
      created <- roomRepo.create(room)
    yield created

  def getRoomsByVenue(venueId: VenueId): IO[DomainError, List[Room]] =
    roomRepo.findByVenue(venueId)

  // --- Stations ---

  def createStation(
      roomId: RoomId,
      name: NonEmptyString,
      stationType: StationType
  ): IO[DomainError, Station] =
    val station = Station(
      id = StationId.generate,
      roomId = roomId,
      name = name,
      stationType = stationType,
      status = StationStatus.Available
    )
    stationRepo.create(station)

  def getStationsByRoom(roomId: RoomId): IO[DomainError, List[Station]] =
    stationRepo.findByRoom(roomId)

  // --- Service Definitions ---

  def createServiceDefinition(
      name: NonEmptyString,
      category: ServiceCategory,
      durationMinutes: PositiveInt,
      capacity: PositiveInt,
      requiresStation: Boolean,
      price: Money
  ): IO[DomainError, ServiceDefinition] =
    val serviceDef = ServiceDefinition(
      id = ServiceDefinitionId.generate,
      name = name,
      category = category,
      durationMinutes = durationMinutes,
      capacity = capacity,
      requiresStation = requiresStation,
      price = price,
      createdAt = Instant.now()
    )
    serviceDefRepo.create(serviceDef)

  def getServiceDefinition(id: ServiceDefinitionId): IO[DomainError, ServiceDefinition] =
    serviceDefRepo.findById(id).flatMap {
      case Some(sd) => ZIO.succeed(sd)
      case None     => ZIO.fail(DomainError.notFound("ServiceDefinition", id.value))
    }

  def listServiceDefinitions: IO[DomainError, List[ServiceDefinition]] = serviceDefRepo.findAll

object CatalogService:
  val layer: URLayer[VenueRepository & RoomRepository & StationRepository & ServiceDefinitionRepository, CatalogService] =
    ZLayer.fromFunction(CatalogService.apply)
