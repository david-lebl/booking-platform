package com.bookingplatform.core.catalog

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import zio.*

trait VenueRepository:
  def create(venue: Venue): IO[DomainError, Venue]
  def findById(id: VenueId): IO[DomainError, Option[Venue]]
  def findAll: IO[DomainError, List[Venue]]
  def update(venue: Venue): IO[DomainError, Venue]

trait RoomRepository:
  def create(room: Room): IO[DomainError, Room]
  def findById(id: RoomId): IO[DomainError, Option[Room]]
  def findByVenue(venueId: VenueId): IO[DomainError, List[Room]]
  def update(room: Room): IO[DomainError, Room]

trait StationRepository:
  def create(station: Station): IO[DomainError, Station]
  def findById(id: StationId): IO[DomainError, Option[Station]]
  def findByRoom(roomId: RoomId): IO[DomainError, List[Station]]
  def update(station: Station): IO[DomainError, Station]

trait ServiceDefinitionRepository:
  def create(serviceDef: ServiceDefinition): IO[DomainError, ServiceDefinition]
  def findById(id: ServiceDefinitionId): IO[DomainError, Option[ServiceDefinition]]
  def findAll: IO[DomainError, List[ServiceDefinition]]
  def findByCategory(category: com.bookingplatform.shared.models.ServiceCategory): IO[DomainError, List[ServiceDefinition]]
