package com.booking.infrastructure.repository

import com.booking.domain.model.*
import com.booking.domain.repository.InstructorRepository
import zio.*

final class InMemoryInstructorRepository(ref: Ref[Map[InstructorId, Instructor]]) extends InstructorRepository:

  def save(instructor: Instructor): Task[Instructor] =
    ref.update(_ + (instructor.id -> instructor)).as(instructor)

  def findById(id: InstructorId): Task[Option[Instructor]] =
    ref.get.map(_.get(id))

  def findAll: Task[List[Instructor]] =
    ref.get.map(_.values.toList)

  def update(instructor: Instructor): Task[Instructor] =
    ref.update(_ + (instructor.id -> instructor)).as(instructor)

object InMemoryInstructorRepository:
  val layer: ZLayer[Any, Nothing, InstructorRepository] =
    ZLayer.fromZIO(
      Ref.make(Map.empty[InstructorId, Instructor]).map(new InMemoryInstructorRepository(_))
    )
