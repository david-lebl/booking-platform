package com.booking.infrastructure.repository

import com.booking.domain.model.*
import com.booking.domain.repository.MemberRepository
import zio.*

final class InMemoryMemberRepository(ref: Ref[Map[MemberId, Member]]) extends MemberRepository:

  def save(member: Member): Task[Member] =
    ref.update(_ + (member.id -> member)).as(member)

  def findById(id: MemberId): Task[Option[Member]] =
    ref.get.map(_.get(id))

  def findByEmail(email: Email): Task[Option[Member]] =
    ref.get.map(_.values.find(_.email == email))

  def findAll: Task[List[Member]] =
    ref.get.map(_.values.toList)

  def update(member: Member): Task[Member] =
    ref.update(_ + (member.id -> member)).as(member)

  def delete(id: MemberId): Task[Unit] =
    ref.update(_ - id)

object InMemoryMemberRepository:
  val layer: ZLayer[Any, Nothing, MemberRepository] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[MemberId, Member]).map(new InMemoryMemberRepository(_))
    )
