package com.booking.domain.repository

import com.booking.domain.model.*
import zio.*
import java.time.Instant

trait ClassSessionRepository:
  def save(session: ClassSession): Task[ClassSession]
  def findById(id: ClassSessionId): Task[Option[ClassSession]]
  def findAll: Task[List[ClassSession]]
  def findByDateRange(from: Instant, to: Instant): Task[List[ClassSession]]
  def update(session: ClassSession): Task[ClassSession]

object ClassSessionRepository:
  def save(session: ClassSession): ZIO[ClassSessionRepository, Throwable, ClassSession] =
    ZIO.serviceWithZIO(_.save(session))
  def findById(id: ClassSessionId): ZIO[ClassSessionRepository, Throwable, Option[ClassSession]] =
    ZIO.serviceWithZIO(_.findById(id))
  def findAll: ZIO[ClassSessionRepository, Throwable, List[ClassSession]] =
    ZIO.serviceWithZIO(_.findAll)
  def findByDateRange(from: Instant, to: Instant): ZIO[ClassSessionRepository, Throwable, List[ClassSession]] =
    ZIO.serviceWithZIO(_.findByDateRange(from, to))
  def update(session: ClassSession): ZIO[ClassSessionRepository, Throwable, ClassSession] =
    ZIO.serviceWithZIO(_.update(session))
