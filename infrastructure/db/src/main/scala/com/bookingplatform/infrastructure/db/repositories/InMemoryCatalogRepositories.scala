package com.bookingplatform.infrastructure.db.repositories

import com.bookingplatform.core.catalog.*
import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.ServiceCategory
import zio.*

final case class InMemoryVenueRepository(ref: Ref[Map[VenueId, Venue]]) extends VenueRepository:
  def create(venue: Venue): IO[DomainError, Venue] =
    ref.update(_ + (venue.id -> venue)).as(venue)

  def findById(id: VenueId): IO[DomainError, Option[Venue]] =
    ref.get.map(_.get(id))

  def findAll: IO[DomainError, List[Venue]] =
    ref.get.map(_.values.toList)

  def update(venue: Venue): IO[DomainError, Venue] =
    ref.update(_ + (venue.id -> venue)).as(venue)

object InMemoryVenueRepository:
  val layer: ULayer[VenueRepository] = ZLayer {
    Ref.make(Map.empty[VenueId, Venue]).map(InMemoryVenueRepository(_))
  }

final case class InMemoryRoomRepository(ref: Ref[Map[RoomId, Room]]) extends RoomRepository:
  def create(room: Room): IO[DomainError, Room] =
    ref.update(_ + (room.id -> room)).as(room)

  def findById(id: RoomId): IO[DomainError, Option[Room]] =
    ref.get.map(_.get(id))

  def findByVenue(venueId: VenueId): IO[DomainError, List[Room]] =
    ref.get.map(_.values.filter(_.venueId == venueId).toList)

  def update(room: Room): IO[DomainError, Room] =
    ref.update(_ + (room.id -> room)).as(room)

object InMemoryRoomRepository:
  val layer: ULayer[RoomRepository] = ZLayer {
    Ref.make(Map.empty[RoomId, Room]).map(InMemoryRoomRepository(_))
  }

final case class InMemoryStationRepository(ref: Ref[Map[StationId, Station]]) extends StationRepository:
  def create(station: Station): IO[DomainError, Station] =
    ref.update(_ + (station.id -> station)).as(station)

  def findById(id: StationId): IO[DomainError, Option[Station]] =
    ref.get.map(_.get(id))

  def findByRoom(roomId: RoomId): IO[DomainError, List[Station]] =
    ref.get.map(_.values.filter(_.roomId == roomId).toList)

  def update(station: Station): IO[DomainError, Station] =
    ref.update(_ + (station.id -> station)).as(station)

object InMemoryStationRepository:
  val layer: ULayer[StationRepository] = ZLayer {
    Ref.make(Map.empty[StationId, Station]).map(InMemoryStationRepository(_))
  }

final case class InMemoryServiceDefinitionRepository(ref: Ref[Map[ServiceDefinitionId, ServiceDefinition]])
    extends ServiceDefinitionRepository:
  def create(sd: ServiceDefinition): IO[DomainError, ServiceDefinition] =
    ref.update(_ + (sd.id -> sd)).as(sd)

  def findById(id: ServiceDefinitionId): IO[DomainError, Option[ServiceDefinition]] =
    ref.get.map(_.get(id))

  def findAll: IO[DomainError, List[ServiceDefinition]] =
    ref.get.map(_.values.toList)

  def findByCategory(category: ServiceCategory): IO[DomainError, List[ServiceDefinition]] =
    ref.get.map(_.values.filter(_.category == category).toList)

object InMemoryServiceDefinitionRepository:
  val layer: ULayer[ServiceDefinitionRepository] = ZLayer {
    Ref.make(Map.empty[ServiceDefinitionId, ServiceDefinition]).map(InMemoryServiceDefinitionRepository(_))
  }
