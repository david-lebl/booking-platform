package com.booking.infrastructure.repository

import com.booking.domain.model.*
import com.booking.domain.repository.StudioRepository
import zio.*

final class InMemoryStudioRepository(ref: Ref[Map[StudioId, Studio]]) extends StudioRepository:

  def save(studio: Studio): Task[Studio] =
    ref.update(_ + (studio.id -> studio)).as(studio)

  def findById(id: StudioId): Task[Option[Studio]] =
    ref.get.map(_.get(id))

  def findAll: Task[List[Studio]] =
    ref.get.map(_.values.toList)

object InMemoryStudioRepository:
  val layer: ZLayer[Any, Nothing, StudioRepository] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[StudioId, Studio]).map(new InMemoryStudioRepository(_))
    )
