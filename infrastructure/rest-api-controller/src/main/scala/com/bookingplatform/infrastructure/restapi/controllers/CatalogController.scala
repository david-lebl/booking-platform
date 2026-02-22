package com.bookingplatform.infrastructure.restapi.controllers

import com.bookingplatform.api.endpoints.CatalogEndpoints
import com.bookingplatform.api.errors.{ApiError, ErrorMapping}
import com.bookingplatform.core.catalog.CatalogService
import com.bookingplatform.core.common.*
import com.bookingplatform.infrastructure.restapi.controllers.ValidationHelper.validate
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*
import sttp.tapir.ztapir.*
import zio.*

object CatalogController:

  def endpoints(catalogService: CatalogService) = List(
    createVenue(catalogService),
    getVenue(catalogService),
    listVenues(catalogService),
    updateVenue(catalogService),
    createRoom(catalogService),
    listRoomsByVenue(catalogService),
    createStation(catalogService),
    listStationsByRoom(catalogService),
    createServiceDefinition(catalogService),
    listServiceDefinitions(catalogService)
  )

  private def createVenue(svc: CatalogService) =
    CatalogEndpoints.createVenue.zServerLogic { req =>
      (for
        name     <- validate(NonEmptyString.make(req.name))
        address  <- validate(NonEmptyString.make(req.address))
        timezone <- validate(Timezone.make(req.timezone))
        venue    <- svc.createVenue(name, address, timezone)
      yield VenueResponse(
        venue.id.value, NonEmptyString.unwrap(venue.name), NonEmptyString.unwrap(venue.address),
        Timezone.unwrap(venue.timezone), venue.status, venue.createdAt, venue.updatedAt
      )).mapError(ErrorMapping.toApiError)
    }

  private def getVenue(svc: CatalogService) =
    CatalogEndpoints.getVenue.zServerLogic { venueId =>
      svc.getVenue(VenueId(venueId)).map(v =>
        VenueResponse(v.id.value, NonEmptyString.unwrap(v.name), NonEmptyString.unwrap(v.address),
          Timezone.unwrap(v.timezone), v.status, v.createdAt, v.updatedAt)
      ).mapError(ErrorMapping.toApiError)
    }

  private def listVenues(svc: CatalogService) =
    CatalogEndpoints.listVenues.zServerLogic { _ =>
      svc.listVenues.map(_.map(v =>
        VenueResponse(v.id.value, NonEmptyString.unwrap(v.name), NonEmptyString.unwrap(v.address),
          Timezone.unwrap(v.timezone), v.status, v.createdAt, v.updatedAt)
      )).mapError(ErrorMapping.toApiError)
    }

  private def updateVenue(svc: CatalogService) =
    CatalogEndpoints.updateVenue.zServerLogic { case (venueId, req) =>
      (for
        name     <- ZIO.foreach(req.name)(n => validate(NonEmptyString.make(n)))
        address  <- ZIO.foreach(req.address)(a => validate(NonEmptyString.make(a)))
        timezone <- ZIO.foreach(req.timezone)(t => validate(Timezone.make(t)))
        venue    <- svc.updateVenue(VenueId(venueId), name, address, timezone, req.status)
      yield VenueResponse(venue.id.value, NonEmptyString.unwrap(venue.name), NonEmptyString.unwrap(venue.address),
        Timezone.unwrap(venue.timezone), venue.status, venue.createdAt, venue.updatedAt)
      ).mapError(ErrorMapping.toApiError)
    }

  private def createRoom(svc: CatalogService) =
    CatalogEndpoints.createRoom.zServerLogic { req =>
      (for
        name     <- validate(NonEmptyString.make(req.name))
        capacity <- validate(PositiveInt.make(req.capacity))
        room     <- svc.createRoom(VenueId(req.venueId), name, capacity, req.roomType, req.stations.getOrElse(0))
      yield RoomResponse(room.id.value, room.venueId.value, NonEmptyString.unwrap(room.name),
        PositiveInt.unwrap(room.capacity), room.roomType, room.stations, room.createdAt)
      ).mapError(ErrorMapping.toApiError)
    }

  private def listRoomsByVenue(svc: CatalogService) =
    CatalogEndpoints.listRoomsByVenue.zServerLogic { venueId =>
      svc.getRoomsByVenue(VenueId(venueId)).map(_.map(r =>
        RoomResponse(r.id.value, r.venueId.value, NonEmptyString.unwrap(r.name),
          PositiveInt.unwrap(r.capacity), r.roomType, r.stations, r.createdAt)
      )).mapError(ErrorMapping.toApiError)
    }

  private def createStation(svc: CatalogService) =
    CatalogEndpoints.createStation.zServerLogic { req =>
      (for
        name    <- validate(NonEmptyString.make(req.name))
        station <- svc.createStation(RoomId(req.roomId), name, req.stationType)
      yield StationResponse(station.id.value, station.roomId.value, NonEmptyString.unwrap(station.name),
        station.stationType, station.status)
      ).mapError(ErrorMapping.toApiError)
    }

  private def listStationsByRoom(svc: CatalogService) =
    CatalogEndpoints.listStationsByRoom.zServerLogic { roomId =>
      svc.getStationsByRoom(RoomId(roomId)).map(_.map(s =>
        StationResponse(s.id.value, s.roomId.value, NonEmptyString.unwrap(s.name),
          s.stationType, s.status)
      )).mapError(ErrorMapping.toApiError)
    }

  private def createServiceDefinition(svc: CatalogService) =
    CatalogEndpoints.createServiceDefinition.zServerLogic { req =>
      (for
        name     <- validate(NonEmptyString.make(req.name))
        duration <- validate(PositiveInt.make(req.durationMinutes))
        capacity <- validate(PositiveInt.make(req.capacity))
        currency <- ZIO.fromEither(Currency.values.find(_.toString == req.priceCurrency)
          .toRight(DomainError.ValidationError(s"Invalid currency: ${req.priceCurrency}")))
        sd       <- svc.createServiceDefinition(name, req.category, duration, capacity, req.requiresStation,
          Money(req.priceAmount, currency))
      yield ServiceDefinitionResponse(sd.id.value, NonEmptyString.unwrap(sd.name), sd.category,
        PositiveInt.unwrap(sd.durationMinutes), PositiveInt.unwrap(sd.capacity), sd.requiresStation,
        sd.price.amount, sd.price.currency.toString, sd.createdAt)
      ).mapError(ErrorMapping.toApiError)
    }

  private def listServiceDefinitions(svc: CatalogService) =
    CatalogEndpoints.listServiceDefinitions.zServerLogic { _ =>
      svc.listServiceDefinitions.map(_.map(sd =>
        ServiceDefinitionResponse(sd.id.value, NonEmptyString.unwrap(sd.name), sd.category,
          PositiveInt.unwrap(sd.durationMinutes), PositiveInt.unwrap(sd.capacity), sd.requiresStation,
          sd.price.amount, sd.price.currency.toString, sd.createdAt)
      )).mapError(ErrorMapping.toApiError)
    }
