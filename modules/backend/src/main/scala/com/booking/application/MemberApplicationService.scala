package com.booking.application

import com.booking.domain.model.*
import com.booking.domain.repository.MemberRepository
import zio.*

case class RegisterMemberCommand(
  firstName: String,
  lastName: String,
  email: String,
  phone: Option[String],
  membershipType: MembershipType,
)

case class MemberApplicationService(memberRepo: MemberRepository):

  def registerMember(cmd: RegisterMemberCommand): Task[Member] =
    for
      _ <- memberRepo.findByEmail(Email(cmd.email)).flatMap {
             case Some(_) => ZIO.fail(new IllegalArgumentException(s"Email ${cmd.email} is already registered"))
             case None    => ZIO.unit
           }
      member = Member.create(
                 name = FullName(cmd.firstName, cmd.lastName),
                 email = Email(cmd.email),
                 phone = cmd.phone.map(PhoneNumber.apply),
                 membershipType = cmd.membershipType,
               )
      saved <- memberRepo.save(member)
    yield saved

  def getMember(id: MemberId): Task[Option[Member]] =
    memberRepo.findById(id)

  def listMembers: Task[List[Member]] =
    memberRepo.findAll

  def deactivateMember(id: MemberId): Task[Member] =
    for
      member <- memberRepo.findById(id).flatMap {
                  case Some(m) => ZIO.succeed(m)
                  case None    => ZIO.fail(new NoSuchElementException(s"Member not found: $id"))
                }
      updated <- memberRepo.update(member.copy(active = false))
    yield updated

object MemberApplicationService:
  val layer: ZLayer[MemberRepository, Nothing, MemberApplicationService] =
    ZLayer.fromFunction(MemberApplicationService.apply)
