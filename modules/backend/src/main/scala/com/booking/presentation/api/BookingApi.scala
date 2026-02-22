package com.booking.presentation.api

import com.booking.application.*
import com.booking.domain.model.*
import com.booking.presentation.dto.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import zio.*
import java.util.UUID

class BookingApi(service: BookingApplicationService):

  private val base = endpoint.in("api" / "bookings").errorOut(jsonBody[ErrorResponse])

  val createEndpoint =
    base.post
      .in(jsonBody[CreateBookingRequest])
      .out(jsonBody[BookingResponse])
      .zServerLogic { req =>
        ZIO.attempt {
          CreateBookingCommand(
            memberId       = MemberId(UUID.fromString(req.memberId)),
            classSessionId = ClassSessionId(UUID.fromString(req.classSessionId)),
          )
        }
        .flatMap(service.createBooking)
        .map(BookingResponse.from)
        .mapError(e => ErrorResponse(e.getMessage))
      }

  val getEndpoint =
    base.get
      .in(path[String]("id"))
      .out(jsonBody[BookingResponse])
      .zServerLogic { idStr =>
        ZIO.attempt(UUID.fromString(idStr))
          .flatMap(uuid => service.getBooking(BookingId(uuid)))
          .flatMap {
            case Some(b) => ZIO.succeed(BookingResponse.from(b))
            case None    => ZIO.fail(new NoSuchElementException(s"Booking not found: $idStr"))
          }
          .mapError(e => ErrorResponse(e.getMessage))
      }

  val cancelEndpoint =
    base.delete
      .in(path[String]("id"))
      .in(query[String]("memberId"))
      .out(jsonBody[BookingResponse])
      .zServerLogic { case (idStr, memberIdStr) =>
        ZIO.attempt {
          (BookingId(UUID.fromString(idStr)), MemberId(UUID.fromString(memberIdStr)))
        }
        .flatMap { case (bookingId, memberId) => service.cancelBooking(bookingId, memberId) }
        .map(BookingResponse.from)
        .mapError(e => ErrorResponse(e.getMessage))
      }

  val memberBookingsEndpoint =
    endpoint.get
      .in("api" / "members" / path[String]("memberId") / "bookings")
      .errorOut(jsonBody[ErrorResponse])
      .out(jsonBody[List[BookingResponse]])
      .zServerLogic { memberIdStr =>
        ZIO.attempt(UUID.fromString(memberIdStr))
          .flatMap(uuid => service.getMemberBookings(MemberId(uuid)))
          .map(_.map(BookingResponse.from))
          .mapError(e => ErrorResponse(e.getMessage))
      }

  val routes = List(createEndpoint, getEndpoint, cancelEndpoint, memberBookingsEndpoint)

object BookingApi:
  val layer: ZLayer[BookingApplicationService, Nothing, BookingApi] =
    ZLayer.fromFunction(BookingApi.apply)
