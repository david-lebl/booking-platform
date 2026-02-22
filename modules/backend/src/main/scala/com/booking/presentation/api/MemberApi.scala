package com.booking.presentation.api

import com.booking.application.*
import com.booking.domain.model.*
import com.booking.presentation.dto.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.server.ziohttp.*
import zio.*
import java.util.UUID

class MemberApi(service: MemberApplicationService):

  private val baseEndpoint = endpoint.in("api" / "members").errorOut(jsonBody[ErrorResponse])

  val registerEndpoint =
    baseEndpoint.post
      .in(jsonBody[RegisterMemberRequest])
      .out(jsonBody[MemberResponse])
      .zServerLogic { req =>
        val membershipType = req.membershipType match
          case "Premium" => MembershipType.Premium
          case "Basic"   => MembershipType.Basic
          case _         => MembershipType.Free
        service.registerMember(RegisterMemberCommand(
          firstName      = req.firstName,
          lastName       = req.lastName,
          email          = req.email,
          phone          = req.phone,
          membershipType = membershipType,
        )).map(MemberResponse.from).mapError(e => ErrorResponse(e.getMessage))
      }

  val getByIdEndpoint =
    baseEndpoint.get
      .in(path[String]("id"))
      .out(jsonBody[MemberResponse])
      .zServerLogic { idStr =>
        ZIO.attempt(UUID.fromString(idStr))
          .flatMap(uuid => service.getMember(MemberId(uuid)))
          .flatMap {
            case Some(m) => ZIO.succeed(MemberResponse.from(m))
            case None    => ZIO.fail(new NoSuchElementException(s"Member not found: $idStr"))
          }
          .mapError(e => ErrorResponse(e.getMessage))
      }

  val listEndpoint =
    baseEndpoint.get
      .out(jsonBody[List[MemberResponse]])
      .zServerLogic { _ =>
        service.listMembers.map(_.map(MemberResponse.from)).mapError(e => ErrorResponse(e.getMessage))
      }

  val routes = List(registerEndpoint, getByIdEndpoint, listEndpoint)

object MemberApi:
  val layer: ZLayer[MemberApplicationService, Nothing, MemberApi] =
    ZLayer.fromFunction(MemberApi.apply)
