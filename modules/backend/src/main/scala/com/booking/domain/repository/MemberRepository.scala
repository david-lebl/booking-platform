package com.booking.domain.repository

import com.booking.domain.model.*
import zio.*

trait MemberRepository:
  def save(member: Member): Task[Member]
  def findById(id: MemberId): Task[Option[Member]]
  def findByEmail(email: Email): Task[Option[Member]]
  def findAll: Task[List[Member]]
  def update(member: Member): Task[Member]
  def delete(id: MemberId): Task[Unit]

object MemberRepository:
  def save(member: Member): ZIO[MemberRepository, Throwable, Member] =
    ZIO.serviceWithZIO(_.save(member))
  def findById(id: MemberId): ZIO[MemberRepository, Throwable, Option[Member]] =
    ZIO.serviceWithZIO(_.findById(id))
  def findByEmail(email: Email): ZIO[MemberRepository, Throwable, Option[Member]] =
    ZIO.serviceWithZIO(_.findByEmail(email))
  def findAll: ZIO[MemberRepository, Throwable, List[Member]] =
    ZIO.serviceWithZIO(_.findAll)
