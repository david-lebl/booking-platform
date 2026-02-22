package com.bookingplatform.core.common

import com.bookingplatform.shared.ids.*
import zio.*
import zio.json.*

import java.time.Instant
import java.util.UUID

trait DomainEvent:
  def eventId: DomainEventId
  def occurredAt: Instant
  def aggregateId: String
  def eventType: String

// --- Booking Events ---

final case class BookingCreated(
    eventId: DomainEventId,
    occurredAt: Instant,
    bookingId: BookingId,
    userId: UserId,
    classInstanceId: ClassInstanceId,
    stationId: Option[StationId]
) extends DomainEvent:
  val aggregateId = bookingId.value.toString
  val eventType   = "BookingCreated"

final case class BookingConfirmed(
    eventId: DomainEventId,
    occurredAt: Instant,
    bookingId: BookingId,
    userId: UserId,
    classInstanceId: ClassInstanceId
) extends DomainEvent:
  val aggregateId = bookingId.value.toString
  val eventType   = "BookingConfirmed"

final case class BookingCancelled(
    eventId: DomainEventId,
    occurredAt: Instant,
    bookingId: BookingId,
    userId: UserId,
    classInstanceId: ClassInstanceId,
    cancellationFee: Option[BigDecimal]
) extends DomainEvent:
  val aggregateId = bookingId.value.toString
  val eventType   = "BookingCancelled"

final case class BookingCheckedIn(
    eventId: DomainEventId,
    occurredAt: Instant,
    bookingId: BookingId,
    userId: UserId,
    classInstanceId: ClassInstanceId
) extends DomainEvent:
  val aggregateId = bookingId.value.toString
  val eventType   = "BookingCheckedIn"

final case class BookingNoShow(
    eventId: DomainEventId,
    occurredAt: Instant,
    bookingId: BookingId,
    userId: UserId,
    classInstanceId: ClassInstanceId
) extends DomainEvent:
  val aggregateId = bookingId.value.toString
  val eventType   = "BookingNoShow"

// --- Waitlist Events ---

final case class WaitlistOffered(
    eventId: DomainEventId,
    occurredAt: Instant,
    waitlistEntryId: WaitlistEntryId,
    userId: UserId,
    classInstanceId: ClassInstanceId,
    expiresAt: Instant
) extends DomainEvent:
  val aggregateId = waitlistEntryId.value.toString
  val eventType   = "WaitlistOffered"

final case class WaitlistConverted(
    eventId: DomainEventId,
    occurredAt: Instant,
    waitlistEntryId: WaitlistEntryId,
    bookingId: BookingId,
    userId: UserId,
    classInstanceId: ClassInstanceId
) extends DomainEvent:
  val aggregateId = waitlistEntryId.value.toString
  val eventType   = "WaitlistConverted"

// --- Event Bus ---

trait EventBus:
  def publish(event: DomainEvent): UIO[Unit]
  def subscribe(handler: DomainEvent => UIO[Unit]): UIO[Unit]

object EventBus:
  def publish(event: DomainEvent): ZIO[EventBus, Nothing, Unit] =
    ZIO.serviceWithZIO[EventBus](_.publish(event))

  def subscribe(handler: DomainEvent => UIO[Unit]): ZIO[EventBus, Nothing, Unit] =
    ZIO.serviceWithZIO[EventBus](_.subscribe(handler))

  val inMemory: ULayer[EventBus] = ZLayer {
    for
      handlers <- Ref.make(List.empty[DomainEvent => UIO[Unit]])
    yield new EventBus:
      def publish(event: DomainEvent): UIO[Unit] =
        handlers.get.flatMap(hs => ZIO.foreachDiscard(hs)(h => h(event)))

      def subscribe(handler: DomainEvent => UIO[Unit]): UIO[Unit] =
        handlers.update(_ :+ handler)
  }
