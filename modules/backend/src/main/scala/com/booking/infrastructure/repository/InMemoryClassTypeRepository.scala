package com.booking.infrastructure.repository

import com.booking.domain.model.*
import com.booking.domain.repository.ClassTypeRepository
import zio.*

final class InMemoryClassTypeRepository(ref: Ref[Map[ClassTypeId, ClassType]]) extends ClassTypeRepository:

  def save(classType: ClassType): Task[ClassType] =
    ref.update(_ + (classType.id -> classType)).as(classType)

  def findById(id: ClassTypeId): Task[Option[ClassType]] =
    ref.get.map(_.get(id))

  def findAll: Task[List[ClassType]] =
    ref.get.map(_.values.toList)

object InMemoryClassTypeRepository:
  val layer: ZLayer[Any, Nothing, ClassTypeRepository] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[ClassTypeId, ClassType]).map(new InMemoryClassTypeRepository(_))
    )
