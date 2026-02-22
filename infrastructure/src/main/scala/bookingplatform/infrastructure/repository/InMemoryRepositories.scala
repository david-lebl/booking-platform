package bookingplatform.infrastructure.repository

import bookingplatform.core.domain.*
import bookingplatform.core.port.*
import zio.*
import java.time.Instant
import java.util.UUID

// ─── In-Memory User Repository ────────────────────
case class InMemoryUserRepository(ref: Ref[Map[UserId, User]]) extends UserRepository:
  def create(user: User): Task[User] =
    ref.update(_ + (user.id -> user)).as(user)
  def findById(id: UserId): Task[Option[User]] =
    ref.get.map(_.get(id))
  def findByEmail(email: String): Task[Option[User]] =
    ref.get.map(_.values.find(_.email == email))
  def findAll(): Task[List[User]] =
    ref.get.map(_.values.toList)
  def update(user: User): Task[User] =
    ref.update(_ + (user.id -> user)).as(user)
  def delete(id: UserId): Task[Boolean] =
    ref.modify(m => (m.contains(id), m - id))

object InMemoryUserRepository:
  val layer: ULayer[UserRepository] = ZLayer {
    Ref.make(Map.empty[UserId, User]).map(InMemoryUserRepository(_))
  }

// ─── In-Memory Trainer Repository ────────────────────
case class InMemoryTrainerRepository(ref: Ref[Map[TrainerId, Trainer]]) extends TrainerRepository:
  def create(trainer: Trainer): Task[Trainer] =
    ref.update(_ + (trainer.id -> trainer)).as(trainer)
  def findById(id: TrainerId): Task[Option[Trainer]] =
    ref.get.map(_.get(id))
  def findAll(): Task[List[Trainer]] =
    ref.get.map(_.values.toList)
  def update(trainer: Trainer): Task[Trainer] =
    ref.update(_ + (trainer.id -> trainer)).as(trainer)
  def delete(id: TrainerId): Task[Boolean] =
    ref.modify(m => (m.contains(id), m - id))

object InMemoryTrainerRepository:
  val layer: ULayer[TrainerRepository] = ZLayer {
    Ref.make(Map.empty[TrainerId, Trainer]).map(InMemoryTrainerRepository(_))
  }

// ─── In-Memory Session Repository ────────────────────
case class InMemorySessionRepository(ref: Ref[Map[SessionId, Session]]) extends SessionRepository:
  def create(session: Session): Task[Session] =
    ref.update(_ + (session.id -> session)).as(session)
  def findById(id: SessionId): Task[Option[Session]] =
    ref.get.map(_.get(id))
  def findAll(): Task[List[Session]] =
    ref.get.map(_.values.toList)
  def findByType(sessionType: SessionType): Task[List[Session]] =
    ref.get.map(_.values.filter(_.sessionType == sessionType).toList)
  def findByTrainer(trainerId: TrainerId): Task[List[Session]] =
    ref.get.map(_.values.filter(_.trainerId == trainerId).toList)
  def update(session: Session): Task[Session] =
    ref.update(_ + (session.id -> session)).as(session)
  def delete(id: SessionId): Task[Boolean] =
    ref.modify(m => (m.contains(id), m - id))

object InMemorySessionRepository:
  val layer: ULayer[SessionRepository] = ZLayer {
    Ref.make(Map.empty[SessionId, Session]).map(InMemorySessionRepository(_))
  }

// ─── In-Memory Scheduled Session Repository ────────────────────
case class InMemoryScheduledSessionRepository(ref: Ref[Map[ScheduledSessionId, ScheduledSession]]) extends ScheduledSessionRepository:
  def create(scheduled: ScheduledSession): Task[ScheduledSession] =
    ref.update(_ + (scheduled.id -> scheduled)).as(scheduled)
  def findById(id: ScheduledSessionId): Task[Option[ScheduledSession]] =
    ref.get.map(_.get(id))
  def findAll(): Task[List[ScheduledSession]] =
    ref.get.map(_.values.toList)
  def findBySession(sessionId: SessionId): Task[List[ScheduledSession]] =
    ref.get.map(_.values.filter(_.sessionId == sessionId).toList)
  def findUpcoming(): Task[List[ScheduledSession]] =
    for
      now  <- Clock.instant
      list <- ref.get.map(_.values.filter(s => s.startTime.isAfter(now) && s.status == ScheduledSessionStatus.Scheduled).toList)
    yield list.sortBy(_.startTime)
  def update(scheduled: ScheduledSession): Task[ScheduledSession] =
    ref.update(_ + (scheduled.id -> scheduled)).as(scheduled)
  def delete(id: ScheduledSessionId): Task[Boolean] =
    ref.modify(m => (m.contains(id), m - id))

object InMemoryScheduledSessionRepository:
  val layer: ULayer[ScheduledSessionRepository] = ZLayer {
    Ref.make(Map.empty[ScheduledSessionId, ScheduledSession]).map(InMemoryScheduledSessionRepository(_))
  }

// ─── In-Memory Booking Repository ────────────────────
case class InMemoryBookingRepository(ref: Ref[Map[BookingId, Booking]]) extends BookingRepository:
  def create(booking: Booking): Task[Booking] =
    ref.update(_ + (booking.id -> booking)).as(booking)
  def findById(id: BookingId): Task[Option[Booking]] =
    ref.get.map(_.get(id))
  def findByUser(userId: UserId): Task[List[Booking]] =
    ref.get.map(_.values.filter(_.userId == userId).toList)
  def findByScheduledSession(scheduledSessionId: ScheduledSessionId): Task[List[Booking]] =
    ref.get.map(_.values.filter(_.scheduledSessionId == scheduledSessionId).toList)
  def findByUserAndScheduledSession(userId: UserId, scheduledSessionId: ScheduledSessionId): Task[Option[Booking]] =
    ref.get.map(_.values.find(b => b.userId == userId && b.scheduledSessionId == scheduledSessionId))
  def update(booking: Booking): Task[Booking] =
    ref.update(_ + (booking.id -> booking)).as(booking)
  def delete(id: BookingId): Task[Boolean] =
    ref.modify(m => (m.contains(id), m - id))

object InMemoryBookingRepository:
  val layer: ULayer[BookingRepository] = ZLayer {
    Ref.make(Map.empty[BookingId, Booking]).map(InMemoryBookingRepository(_))
  }

// ─── In-Memory Subscription Plan Repository ────────────────────
case class InMemorySubscriptionPlanRepository(ref: Ref[Map[SubscriptionPlanId, SubscriptionPlan]]) extends SubscriptionPlanRepository:
  def create(plan: SubscriptionPlan): Task[SubscriptionPlan] =
    ref.update(_ + (plan.id -> plan)).as(plan)
  def findById(id: SubscriptionPlanId): Task[Option[SubscriptionPlan]] =
    ref.get.map(_.get(id))
  def findAll(): Task[List[SubscriptionPlan]] =
    ref.get.map(_.values.toList)
  def update(plan: SubscriptionPlan): Task[SubscriptionPlan] =
    ref.update(_ + (plan.id -> plan)).as(plan)
  def delete(id: SubscriptionPlanId): Task[Boolean] =
    ref.modify(m => (m.contains(id), m - id))

object InMemorySubscriptionPlanRepository:
  val layer: ULayer[SubscriptionPlanRepository] = ZLayer {
    Ref.make(Map.empty[SubscriptionPlanId, SubscriptionPlan]).map(InMemorySubscriptionPlanRepository(_))
  }

// ─── In-Memory Subscription Repository ────────────────────
case class InMemorySubscriptionRepository(ref: Ref[Map[SubscriptionId, Subscription]]) extends SubscriptionRepository:
  def create(subscription: Subscription): Task[Subscription] =
    ref.update(_ + (subscription.id -> subscription)).as(subscription)
  def findById(id: SubscriptionId): Task[Option[Subscription]] =
    ref.get.map(_.get(id))
  def findByUser(userId: UserId): Task[List[Subscription]] =
    ref.get.map(_.values.filter(_.userId == userId).toList)
  def findActiveByUser(userId: UserId): Task[List[Subscription]] =
    ref.get.map(_.values.filter(s => s.userId == userId && s.status == SubscriptionStatus.Active).toList)
  def update(subscription: Subscription): Task[Subscription] =
    ref.update(_ + (subscription.id -> subscription)).as(subscription)
  def delete(id: SubscriptionId): Task[Boolean] =
    ref.modify(m => (m.contains(id), m - id))

object InMemorySubscriptionRepository:
  val layer: ULayer[SubscriptionRepository] = ZLayer {
    Ref.make(Map.empty[SubscriptionId, Subscription]).map(InMemorySubscriptionRepository(_))
  }

// ─── In-Memory Waitlist Repository ────────────────────
case class InMemoryWaitlistRepository(ref: Ref[Map[WaitlistEntryId, WaitlistEntry]]) extends WaitlistRepository:
  def create(entry: WaitlistEntry): Task[WaitlistEntry] =
    ref.update(_ + (entry.id -> entry)).as(entry)
  def findById(id: WaitlistEntryId): Task[Option[WaitlistEntry]] =
    ref.get.map(_.get(id))
  def findByScheduledSession(scheduledSessionId: ScheduledSessionId): Task[List[WaitlistEntry]] =
    ref.get.map(_.values.filter(_.scheduledSessionId == scheduledSessionId).toList.sortBy(_.position))
  def findByUser(userId: UserId): Task[List[WaitlistEntry]] =
    ref.get.map(_.values.filter(_.userId == userId).toList)
  def findByUserAndSession(userId: UserId, scheduledSessionId: ScheduledSessionId): Task[Option[WaitlistEntry]] =
    ref.get.map(_.values.find(e => e.userId == userId && e.scheduledSessionId == scheduledSessionId))
  def removeByUserAndSession(userId: UserId, scheduledSessionId: ScheduledSessionId): Task[Boolean] =
    ref.modify { m =>
      val entry = m.values.find(e => e.userId == userId && e.scheduledSessionId == scheduledSessionId)
      entry match
        case Some(e) => (true, m - e.id)
        case None    => (false, m)
    }
  def delete(id: WaitlistEntryId): Task[Boolean] =
    ref.modify(m => (m.contains(id), m - id))

object InMemoryWaitlistRepository:
  val layer: ULayer[WaitlistRepository] = ZLayer {
    Ref.make(Map.empty[WaitlistEntryId, WaitlistEntry]).map(InMemoryWaitlistRepository(_))
  }

// ─── In-Memory Cancellation Repository ────────────────────
case class InMemoryCancellationRepository(ref: Ref[Map[CancellationId, Cancellation]]) extends CancellationRepository:
  def create(cancellation: Cancellation): Task[Cancellation] =
    ref.update(_ + (cancellation.id -> cancellation)).as(cancellation)
  def findByBooking(bookingId: BookingId): Task[Option[Cancellation]] =
    ref.get.map(_.values.find(_.bookingId == bookingId))
  def findByUser(userId: UserId): Task[List[Cancellation]] =
    ref.get.map(_.values.filter(_.userId == userId).toList)

object InMemoryCancellationRepository:
  val layer: ULayer[CancellationRepository] = ZLayer {
    Ref.make(Map.empty[CancellationId, Cancellation]).map(InMemoryCancellationRepository(_))
  }

// ─── In-Memory Payment Repository ────────────────────
case class InMemoryPaymentRepository(ref: Ref[Map[PaymentId, Payment]]) extends PaymentRepository:
  def create(payment: Payment): Task[Payment] =
    ref.update(_ + (payment.id -> payment)).as(payment)
  def findById(id: PaymentId): Task[Option[Payment]] =
    ref.get.map(_.get(id))
  def findByUser(userId: UserId): Task[List[Payment]] =
    ref.get.map(_.values.filter(_.userId == userId).toList)
  def update(payment: Payment): Task[Payment] =
    ref.update(_ + (payment.id -> payment)).as(payment)

object InMemoryPaymentRepository:
  val layer: ULayer[PaymentRepository] = ZLayer {
    Ref.make(Map.empty[PaymentId, Payment]).map(InMemoryPaymentRepository(_))
  }
