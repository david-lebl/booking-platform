package com.bookingplatform.core.catalog

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*

import java.time.Instant

final case class Venue(
    id: VenueId,
    name: NonEmptyString,
    address: NonEmptyString,
    timezone: Timezone,
    status: VenueStatus,
    createdAt: Instant,
    updatedAt: Instant
)

final case class Room(
    id: RoomId,
    venueId: VenueId,
    name: NonEmptyString,
    capacity: PositiveInt,
    roomType: RoomType,
    stations: Int,
    createdAt: Instant
)

final case class Station(
    id: StationId,
    roomId: RoomId,
    name: NonEmptyString,
    stationType: StationType,
    status: StationStatus
)

final case class ServiceDefinition(
    id: ServiceDefinitionId,
    name: NonEmptyString,
    category: ServiceCategory,
    durationMinutes: PositiveInt,
    capacity: PositiveInt,
    requiresStation: Boolean,
    price: Money,
    createdAt: Instant
)
