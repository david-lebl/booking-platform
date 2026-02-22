package com.bookingplatform.core.booking

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import zio.*

trait BookingRepository:
  def create(booking: Booking): IO[DomainError, Booking]
  def findById(id: BookingId): IO[DomainError, Option[Booking]]
  def findByUser(userId: UserId): IO[DomainError, List[Booking]]
  def findByClassInstance(classInstanceId: ClassInstanceId): IO[DomainError, List[Booking]]
  def findByUserAndClassInstance(userId: UserId, classInstanceId: ClassInstanceId): IO[DomainError, Option[Booking]]
  def update(booking: Booking): IO[DomainError, Booking]
  def countByClassInstance(classInstanceId: ClassInstanceId): IO[DomainError, Int]

trait WaitlistRepository:
  def create(entry: WaitlistEntry): IO[DomainError, WaitlistEntry]
  def findById(id: WaitlistEntryId): IO[DomainError, Option[WaitlistEntry]]
  def findByClassInstance(classInstanceId: ClassInstanceId): IO[DomainError, List[WaitlistEntry]]
  def findByUser(userId: UserId): IO[DomainError, List[WaitlistEntry]]
  def findNextWaiting(classInstanceId: ClassInstanceId): IO[DomainError, Option[WaitlistEntry]]
  def update(entry: WaitlistEntry): IO[DomainError, WaitlistEntry]
  def countByClassInstance(classInstanceId: ClassInstanceId): IO[DomainError, Int]

trait CancellationPolicyRepository:
  def create(policy: CancellationPolicy): IO[DomainError, CancellationPolicy]
  def findById(id: CancellationPolicyId): IO[DomainError, Option[CancellationPolicy]]
  def findAll: IO[DomainError, List[CancellationPolicy]]
