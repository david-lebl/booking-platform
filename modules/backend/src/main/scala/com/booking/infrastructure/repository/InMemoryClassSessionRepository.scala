package com.booking.infrastructure.repository

import com.booking.domain.model.*
import com.booking.domain.repository.ClassSessionRepository
import zio.*
import java.time.Instant

final class InMemoryClassSessionRepository(ref: Ref[Map[ClassSessionId, ClassSession]]) extends ClassSessionRepository:

  def save(session: ClassSession): Task[ClassSession] =
    ref.update(_ + (session.id -> session)).as(session)

  def findById(id: ClassSessionId): Task[Option[ClassSession]] =
    ref.get.map(_.get(id))

  def findAll: Task[List[ClassSession]] =
    ref.get.map(_.values.toList)

  def findByDateRange(from: Instant, to: Instant): Task[List[ClassSession]] =
    ref.get.map(_.values.filter(s => !s.startTime.isBefore(from) && !s.startTime.isAfter(to)).toList)

  def update(session: ClassSession): Task[ClassSession] =
    ref.update(_ + (session.id -> session)).as(session)

object InMemoryClassSessionRepository:
  val layer: ZLayer[Any, Nothing, ClassSessionRepository] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[ClassSessionId, ClassSession]).map(new InMemoryClassSessionRepository(_))
    )
