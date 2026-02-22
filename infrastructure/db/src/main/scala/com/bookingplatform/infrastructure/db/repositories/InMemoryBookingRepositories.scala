package com.bookingplatform.infrastructure.db.repositories

import com.bookingplatform.core.booking.*
import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.{BookingStatus, WaitlistStatus}
import zio.*

final case class InMemoryBookingRepository(ref: Ref[Map[BookingId, Booking]]) extends BookingRepository:
  def create(booking: Booking): IO[DomainError, Booking] =
    ref.update(_ + (booking.id -> booking)).as(booking)

  def findById(id: BookingId): IO[DomainError, Option[Booking]] =
    ref.get.map(_.get(id))

  def findByUser(userId: UserId): IO[DomainError, List[Booking]] =
    ref.get.map(_.values.filter(_.userId == userId).toList)

  def findByClassInstance(classInstanceId: ClassInstanceId): IO[DomainError, List[Booking]] =
    ref.get.map(_.values.filter(_.classInstanceId == classInstanceId).toList)

  def findByUserAndClassInstance(userId: UserId, classInstanceId: ClassInstanceId): IO[DomainError, Option[Booking]] =
    ref.get.map(_.values.find(b => b.userId == userId && b.classInstanceId == classInstanceId))

  def update(booking: Booking): IO[DomainError, Booking] =
    ref.update(_ + (booking.id -> booking)).as(booking)

  def countByClassInstance(classInstanceId: ClassInstanceId): IO[DomainError, Int] =
    ref.get.map(_.values.count(b =>
      b.classInstanceId == classInstanceId && !BookingStateMachine.isTerminal(b.status)
    ))

object InMemoryBookingRepository:
  val layer: ULayer[BookingRepository] = ZLayer {
    Ref.make(Map.empty[BookingId, Booking]).map(InMemoryBookingRepository(_))
  }

final case class InMemoryWaitlistRepository(ref: Ref[Map[WaitlistEntryId, WaitlistEntry]]) extends WaitlistRepository:
  def create(entry: WaitlistEntry): IO[DomainError, WaitlistEntry] =
    ref.update(_ + (entry.id -> entry)).as(entry)

  def findById(id: WaitlistEntryId): IO[DomainError, Option[WaitlistEntry]] =
    ref.get.map(_.get(id))

  def findByClassInstance(classInstanceId: ClassInstanceId): IO[DomainError, List[WaitlistEntry]] =
    ref.get.map(_.values.filter(_.classInstanceId == classInstanceId).toList.sortBy(_.position))

  def findByUser(userId: UserId): IO[DomainError, List[WaitlistEntry]] =
    ref.get.map(_.values.filter(_.userId == userId).toList)

  def findNextWaiting(classInstanceId: ClassInstanceId): IO[DomainError, Option[WaitlistEntry]] =
    ref.get.map(_.values
      .filter(e => e.classInstanceId == classInstanceId && e.status == WaitlistStatus.Waiting)
      .toList
      .sortBy(_.position)
      .headOption
    )

  def update(entry: WaitlistEntry): IO[DomainError, WaitlistEntry] =
    ref.update(_ + (entry.id -> entry)).as(entry)

  def countByClassInstance(classInstanceId: ClassInstanceId): IO[DomainError, Int] =
    ref.get.map(_.values.count(e =>
      e.classInstanceId == classInstanceId &&
        (e.status == WaitlistStatus.Waiting || e.status == WaitlistStatus.Offered)
    ))

object InMemoryWaitlistRepository:
  val layer: ULayer[WaitlistRepository] = ZLayer {
    Ref.make(Map.empty[WaitlistEntryId, WaitlistEntry]).map(InMemoryWaitlistRepository(_))
  }

final case class InMemoryCancellationPolicyRepository(ref: Ref[Map[CancellationPolicyId, CancellationPolicy]])
    extends CancellationPolicyRepository:
  def create(policy: CancellationPolicy): IO[DomainError, CancellationPolicy] =
    ref.update(_ + (policy.id -> policy)).as(policy)

  def findById(id: CancellationPolicyId): IO[DomainError, Option[CancellationPolicy]] =
    ref.get.map(_.get(id))

  def findAll: IO[DomainError, List[CancellationPolicy]] =
    ref.get.map(_.values.toList)

object InMemoryCancellationPolicyRepository:
  val layer: ULayer[CancellationPolicyRepository] = ZLayer {
    Ref.make(Map.empty[CancellationPolicyId, CancellationPolicy]).map(InMemoryCancellationPolicyRepository(_))
  }
