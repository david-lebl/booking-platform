package com.booking.domain.repository

import com.booking.domain.model.*
import zio.*

trait StudioRepository:
  def save(studio: Studio): Task[Studio]
  def findById(id: StudioId): Task[Option[Studio]]
  def findAll: Task[List[Studio]]

object StudioRepository:
  def save(studio: Studio): ZIO[StudioRepository, Throwable, Studio] =
    ZIO.serviceWithZIO(_.save(studio))
  def findById(id: StudioId): ZIO[StudioRepository, Throwable, Option[Studio]] =
    ZIO.serviceWithZIO(_.findById(id))
  def findAll: ZIO[StudioRepository, Throwable, List[Studio]] =
    ZIO.serviceWithZIO(_.findAll)
