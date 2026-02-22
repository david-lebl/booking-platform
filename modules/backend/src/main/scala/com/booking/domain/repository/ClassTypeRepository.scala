package com.booking.domain.repository

import com.booking.domain.model.*
import zio.*

trait ClassTypeRepository:
  def save(classType: ClassType): Task[ClassType]
  def findById(id: ClassTypeId): Task[Option[ClassType]]
  def findAll: Task[List[ClassType]]

object ClassTypeRepository:
  def save(ct: ClassType): ZIO[ClassTypeRepository, Throwable, ClassType] =
    ZIO.serviceWithZIO(_.save(ct))
  def findById(id: ClassTypeId): ZIO[ClassTypeRepository, Throwable, Option[ClassType]] =
    ZIO.serviceWithZIO(_.findById(id))
  def findAll: ZIO[ClassTypeRepository, Throwable, List[ClassType]] =
    ZIO.serviceWithZIO(_.findAll)
