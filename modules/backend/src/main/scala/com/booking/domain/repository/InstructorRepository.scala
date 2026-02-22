package com.booking.domain.repository

import com.booking.domain.model.*
import zio.*

trait InstructorRepository:
  def save(instructor: Instructor): Task[Instructor]
  def findById(id: InstructorId): Task[Option[Instructor]]
  def findAll: Task[List[Instructor]]
  def update(instructor: Instructor): Task[Instructor]

object InstructorRepository:
  def save(instructor: Instructor): ZIO[InstructorRepository, Throwable, Instructor] =
    ZIO.serviceWithZIO(_.save(instructor))
  def findById(id: InstructorId): ZIO[InstructorRepository, Throwable, Option[Instructor]] =
    ZIO.serviceWithZIO(_.findById(id))
  def findAll: ZIO[InstructorRepository, Throwable, List[Instructor]] =
    ZIO.serviceWithZIO(_.findAll)
