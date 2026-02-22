package com.bookingplatform.core.booking

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.WaitlistStatus
import zio.*

import java.time.{Duration, Instant}

final case class WaitlistService(
    waitlistRepo: WaitlistRepository,
    bookingRepo: BookingRepository,
    eventBus: EventBus
):

  private val offerDuration = Duration.ofMinutes(30)

  def joinWaitlist(userId: UserId, classInstanceId: ClassInstanceId): IO[DomainError, WaitlistEntry] =
    for
      existing <- waitlistRepo.findByClassInstance(classInstanceId)
      alreadyOnWaitlist = existing.exists(e =>
        e.userId == userId && (e.status == WaitlistStatus.Waiting || e.status == WaitlistStatus.Offered)
      )
      _ <- ZIO.when(alreadyOnWaitlist)(
        ZIO.fail(DomainError.ConflictError("User is already on the waitlist for this class"))
      )
      position = existing.count(e => e.status == WaitlistStatus.Waiting || e.status == WaitlistStatus.Offered) + 1
      entry = WaitlistEntry(
        id = WaitlistEntryId.generate,
        classInstanceId = classInstanceId,
        userId = userId,
        position = position,
        status = WaitlistStatus.Waiting,
        offerExpiresAt = None,
        createdAt = Instant.now()
      )
      created <- waitlistRepo.create(entry)
    yield created

  def promoteNext(classInstanceId: ClassInstanceId): IO[DomainError, Option[WaitlistEntry]] =
    for
      nextOpt <- waitlistRepo.findNextWaiting(classInstanceId)
      result <- nextOpt match
        case Some(entry) =>
          val now       = Instant.now()
          val expiresAt = now.plus(offerDuration)
          val updated = entry.copy(
            status = WaitlistStatus.Offered,
            offerExpiresAt = Some(expiresAt)
          )
          for
            saved <- waitlistRepo.update(updated)
            _ <- eventBus.publish(WaitlistOffered(
              eventId = DomainEventId.generate,
              occurredAt = now,
              waitlistEntryId = saved.id,
              userId = saved.userId,
              classInstanceId = classInstanceId,
              expiresAt = expiresAt
            ))
          yield Some(saved)
        case None => ZIO.succeed(None)
    yield result

  def acceptOffer(waitlistEntryId: WaitlistEntryId): IO[DomainError, WaitlistEntry] =
    for
      entry <- waitlistRepo.findById(waitlistEntryId).flatMap {
        case Some(e) => ZIO.succeed(e)
        case None    => ZIO.fail(DomainError.notFound("WaitlistEntry", waitlistEntryId.value))
      }
      _ <- ZIO.when(entry.status != WaitlistStatus.Offered)(
        ZIO.fail(DomainError.InvalidStateTransition(entry.status.toString, WaitlistStatus.Converted.toString))
      )
      now = Instant.now()
      _ <- ZIO.when(entry.offerExpiresAt.exists(_.isBefore(now)))(
        ZIO.fail(DomainError.BookingWindowClosed("Waitlist offer has expired"))
      )
      updated = entry.copy(status = WaitlistStatus.Converted)
      result <- waitlistRepo.update(updated)
    yield result

  def removeFromWaitlist(waitlistEntryId: WaitlistEntryId): IO[DomainError, WaitlistEntry] =
    for
      entry <- waitlistRepo.findById(waitlistEntryId).flatMap {
        case Some(e) => ZIO.succeed(e)
        case None    => ZIO.fail(DomainError.notFound("WaitlistEntry", waitlistEntryId.value))
      }
      updated = entry.copy(status = WaitlistStatus.Removed)
      result <- waitlistRepo.update(updated)
    yield result

  def getWaitlist(classInstanceId: ClassInstanceId): IO[DomainError, List[WaitlistEntry]] =
    waitlistRepo.findByClassInstance(classInstanceId)

  def getUserWaitlistEntries(userId: UserId): IO[DomainError, List[WaitlistEntry]] =
    waitlistRepo.findByUser(userId)

object WaitlistService:
  val layer: URLayer[WaitlistRepository & BookingRepository & EventBus, WaitlistService] =
    ZLayer.fromFunction(WaitlistService.apply)
