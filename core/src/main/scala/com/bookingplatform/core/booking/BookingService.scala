package com.bookingplatform.core.booking

import com.bookingplatform.core.common.*
import com.bookingplatform.core.scheduling.{ClassInstance, SchedulingService}
import com.bookingplatform.core.catalog.CatalogService
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.{BookingStatus, ClassInstanceStatus, WaitlistStatus}
import zio.*

import java.time.{Duration, Instant}

final case class BookingService(
    bookingRepo: BookingRepository,
    waitlistRepo: WaitlistRepository,
    cancellationPolicyRepo: CancellationPolicyRepository,
    schedulingService: SchedulingService,
    catalogService: CatalogService,
    eventBus: EventBus
):

  def createBooking(
      userId: UserId,
      classInstanceId: ClassInstanceId,
      stationId: Option[StationId]
  ): IO[DomainError, Booking] =
    for
      instance <- schedulingService.getClassInstance(classInstanceId)
      classDef <- schedulingService.getClassDefinition(instance.classDefinitionId)
      now       = Instant.now()

      // Check booking window
      _ <- ZIO.unless(schedulingService.isWithinBookingWindow(classDef, instance, now))(
        ZIO.fail(DomainError.BookingWindowClosed("Booking window is not open for this class"))
      )

      // Check for duplicate booking
      existing <- bookingRepo.findByUserAndClassInstance(userId, classInstanceId)
      _ <- ZIO.when(existing.exists(b => !BookingStateMachine.isTerminal(b.status)))(
        ZIO.fail(DomainError.DuplicateBooking(userId.value, classInstanceId.value))
      )

      // Check capacity
      _ <- ZIO.when(instance.isFull)(
        ZIO.fail(DomainError.ClassFull(classInstanceId.value))
      )

      // Check station availability if required
      _ <- ZIO.when(stationId.isDefined)(
        for
          stations <- catalogService.getStationsByRoom(classDef.roomId)
          station = stationId.flatMap(sid => stations.find(_.id == sid))
          _ <- ZIO.when(station.isEmpty)(
            ZIO.fail(DomainError.StationUnavailable(stationId.get.value))
          )
        yield ()
      )

      booking = Booking(
        id = BookingId.generate,
        userId = userId,
        classInstanceId = classInstanceId,
        stationId = stationId,
        status = BookingStatus.Confirmed,
        cancellationFee = None,
        createdAt = now,
        updatedAt = now
      )
      created <- bookingRepo.create(booking)
      _ <- eventBus.publish(BookingConfirmed(
        eventId = DomainEventId.generate,
        occurredAt = now,
        bookingId = created.id,
        userId = userId,
        classInstanceId = classInstanceId
      ))
    yield created

  def cancelBooking(bookingId: BookingId, cancelledBy: UserId): IO[DomainError, Booking] =
    for
      booking  <- getBooking(bookingId)
      instance <- schedulingService.getClassInstance(booking.classInstanceId)
      classDef <- schedulingService.getClassDefinition(instance.classDefinitionId)
      now       = Instant.now()

      newStatus <- ZIO.fromEither(BookingStateMachine.transition(booking.status, BookingStatus.Cancelled))

      // Calculate cancellation fee
      fee <- classDef.cancellationPolicyId match
        case Some(policyId) =>
          cancellationPolicyRepo.findById(policyId).map { policyOpt =>
            policyOpt.flatMap { policy =>
              val minutesUntilClass = Duration.between(now, instance.startTime).toMinutes
              policy.rules
                .sortBy(_.noticePeriodMinutes)(Ordering[Int].reverse)
                .find(_.noticePeriodMinutes >= minutesUntilClass)
                .map(_.calculateFee(BigDecimal(0))) // TODO: get actual service price
            }
          }
        case None => ZIO.succeed(None)

      updated = booking.copy(
        status = newStatus,
        cancellationFee = fee,
        updatedAt = now
      )
      result <- bookingRepo.update(updated)
      _ <- eventBus.publish(BookingCancelled(
        eventId = DomainEventId.generate,
        occurredAt = now,
        bookingId = bookingId,
        userId = booking.userId,
        classInstanceId = booking.classInstanceId,
        cancellationFee = fee
      ))
    yield result

  def checkIn(bookingId: BookingId): IO[DomainError, Booking] =
    for
      booking   <- getBooking(bookingId)
      newStatus <- ZIO.fromEither(BookingStateMachine.transition(booking.status, BookingStatus.CheckedIn))
      now        = Instant.now()
      updated    = booking.copy(status = newStatus, updatedAt = now)
      result    <- bookingRepo.update(updated)
      _ <- eventBus.publish(BookingCheckedIn(
        eventId = DomainEventId.generate,
        occurredAt = now,
        bookingId = bookingId,
        userId = booking.userId,
        classInstanceId = booking.classInstanceId
      ))
    yield result

  def markNoShow(bookingId: BookingId): IO[DomainError, Booking] =
    for
      booking   <- getBooking(bookingId)
      newStatus <- ZIO.fromEither(BookingStateMachine.transition(booking.status, BookingStatus.NoShow))
      now        = Instant.now()
      updated    = booking.copy(status = newStatus, updatedAt = now)
      result    <- bookingRepo.update(updated)
      _ <- eventBus.publish(BookingNoShow(
        eventId = DomainEventId.generate,
        occurredAt = now,
        bookingId = bookingId,
        userId = booking.userId,
        classInstanceId = booking.classInstanceId
      ))
    yield result

  def completeBooking(bookingId: BookingId): IO[DomainError, Booking] =
    for
      booking   <- getBooking(bookingId)
      newStatus <- ZIO.fromEither(BookingStateMachine.transition(booking.status, BookingStatus.Completed))
      now        = Instant.now()
      updated    = booking.copy(status = newStatus, updatedAt = now)
      result    <- bookingRepo.update(updated)
    yield result

  def getBooking(id: BookingId): IO[DomainError, Booking] =
    bookingRepo.findById(id).flatMap {
      case Some(b) => ZIO.succeed(b)
      case None    => ZIO.fail(DomainError.notFound("Booking", id.value))
    }

  def getUserBookings(userId: UserId): IO[DomainError, List[Booking]] =
    bookingRepo.findByUser(userId)

  def getClassInstanceBookings(classInstanceId: ClassInstanceId): IO[DomainError, List[Booking]] =
    bookingRepo.findByClassInstance(classInstanceId)

  // --- Cancellation Policy ---

  def createCancellationPolicy(name: NonEmptyString, rules: List[CancellationRule]): IO[DomainError, CancellationPolicy] =
    val policy = CancellationPolicy(
      id = CancellationPolicyId.generate,
      name = name,
      rules = rules.sortBy(_.noticePeriodMinutes)(Ordering[Int].reverse),
      createdAt = Instant.now()
    )
    cancellationPolicyRepo.create(policy)

  def listCancellationPolicies: IO[DomainError, List[CancellationPolicy]] =
    cancellationPolicyRepo.findAll

object BookingService:
  val layer: URLayer[
    BookingRepository & WaitlistRepository & CancellationPolicyRepository & SchedulingService & CatalogService & EventBus,
    BookingService
  ] = ZLayer.fromFunction(BookingService.apply)
