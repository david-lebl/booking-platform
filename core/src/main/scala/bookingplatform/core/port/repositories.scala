package bookingplatform.core.port

import bookingplatform.core.domain.*
import zio.*
import java.util.UUID

trait UserRepository:
  def create(user: User): Task[User]
  def findById(id: UserId): Task[Option[User]]
  def findByEmail(email: String): Task[Option[User]]
  def findAll(): Task[List[User]]
  def update(user: User): Task[User]
  def delete(id: UserId): Task[Boolean]

trait TrainerRepository:
  def create(trainer: Trainer): Task[Trainer]
  def findById(id: TrainerId): Task[Option[Trainer]]
  def findAll(): Task[List[Trainer]]
  def update(trainer: Trainer): Task[Trainer]
  def delete(id: TrainerId): Task[Boolean]

trait SessionRepository:
  def create(session: Session): Task[Session]
  def findById(id: SessionId): Task[Option[Session]]
  def findAll(): Task[List[Session]]
  def findByType(sessionType: SessionType): Task[List[Session]]
  def findByTrainer(trainerId: TrainerId): Task[List[Session]]
  def update(session: Session): Task[Session]
  def delete(id: SessionId): Task[Boolean]

trait ScheduledSessionRepository:
  def create(scheduled: ScheduledSession): Task[ScheduledSession]
  def findById(id: ScheduledSessionId): Task[Option[ScheduledSession]]
  def findAll(): Task[List[ScheduledSession]]
  def findBySession(sessionId: SessionId): Task[List[ScheduledSession]]
  def findUpcoming(): Task[List[ScheduledSession]]
  def update(scheduled: ScheduledSession): Task[ScheduledSession]
  def delete(id: ScheduledSessionId): Task[Boolean]

trait BookingRepository:
  def create(booking: Booking): Task[Booking]
  def findById(id: BookingId): Task[Option[Booking]]
  def findByUser(userId: UserId): Task[List[Booking]]
  def findByScheduledSession(scheduledSessionId: ScheduledSessionId): Task[List[Booking]]
  def findByUserAndScheduledSession(userId: UserId, scheduledSessionId: ScheduledSessionId): Task[Option[Booking]]
  def update(booking: Booking): Task[Booking]
  def delete(id: BookingId): Task[Boolean]

trait SubscriptionPlanRepository:
  def create(plan: SubscriptionPlan): Task[SubscriptionPlan]
  def findById(id: SubscriptionPlanId): Task[Option[SubscriptionPlan]]
  def findAll(): Task[List[SubscriptionPlan]]
  def update(plan: SubscriptionPlan): Task[SubscriptionPlan]
  def delete(id: SubscriptionPlanId): Task[Boolean]

trait SubscriptionRepository:
  def create(subscription: Subscription): Task[Subscription]
  def findById(id: SubscriptionId): Task[Option[Subscription]]
  def findByUser(userId: UserId): Task[List[Subscription]]
  def findActiveByUser(userId: UserId): Task[List[Subscription]]
  def update(subscription: Subscription): Task[Subscription]
  def delete(id: SubscriptionId): Task[Boolean]

trait WaitlistRepository:
  def create(entry: WaitlistEntry): Task[WaitlistEntry]
  def findById(id: WaitlistEntryId): Task[Option[WaitlistEntry]]
  def findByScheduledSession(scheduledSessionId: ScheduledSessionId): Task[List[WaitlistEntry]]
  def findByUser(userId: UserId): Task[List[WaitlistEntry]]
  def findByUserAndSession(userId: UserId, scheduledSessionId: ScheduledSessionId): Task[Option[WaitlistEntry]]
  def removeByUserAndSession(userId: UserId, scheduledSessionId: ScheduledSessionId): Task[Boolean]
  def delete(id: WaitlistEntryId): Task[Boolean]

trait CancellationRepository:
  def create(cancellation: Cancellation): Task[Cancellation]
  def findByBooking(bookingId: BookingId): Task[Option[Cancellation]]
  def findByUser(userId: UserId): Task[List[Cancellation]]

trait PaymentRepository:
  def create(payment: Payment): Task[Payment]
  def findById(id: PaymentId): Task[Option[Payment]]
  def findByUser(userId: UserId): Task[List[Payment]]
  def update(payment: Payment): Task[Payment]
