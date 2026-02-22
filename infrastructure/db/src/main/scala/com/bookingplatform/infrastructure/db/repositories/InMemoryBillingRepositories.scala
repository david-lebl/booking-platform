package com.bookingplatform.infrastructure.db.repositories

import com.bookingplatform.core.billing.*
import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.{CreditPackageStatus, SubscriptionStatus}
import zio.*

final case class InMemorySubscriptionPlanRepository(ref: Ref[Map[SubscriptionPlanId, SubscriptionPlan]])
    extends SubscriptionPlanRepository:
  def create(plan: SubscriptionPlan): IO[DomainError, SubscriptionPlan] =
    ref.update(_ + (plan.id -> plan)).as(plan)

  def findById(id: SubscriptionPlanId): IO[DomainError, Option[SubscriptionPlan]] =
    ref.get.map(_.get(id))

  def findAll: IO[DomainError, List[SubscriptionPlan]] =
    ref.get.map(_.values.toList)

object InMemorySubscriptionPlanRepository:
  val layer: ULayer[SubscriptionPlanRepository] = ZLayer {
    Ref.make(Map.empty[SubscriptionPlanId, SubscriptionPlan]).map(InMemorySubscriptionPlanRepository(_))
  }

final case class InMemorySubscriptionRepository(ref: Ref[Map[SubscriptionId, Subscription]])
    extends SubscriptionRepository:
  def create(sub: Subscription): IO[DomainError, Subscription] =
    ref.update(_ + (sub.id -> sub)).as(sub)

  def findById(id: SubscriptionId): IO[DomainError, Option[Subscription]] =
    ref.get.map(_.get(id))

  def findByUser(userId: UserId): IO[DomainError, List[Subscription]] =
    ref.get.map(_.values.filter(_.userId == userId).toList)

  def findActiveByUser(userId: UserId): IO[DomainError, Option[Subscription]] =
    ref.get.map(_.values.find(s => s.userId == userId && s.status == SubscriptionStatus.Active))

  def update(sub: Subscription): IO[DomainError, Subscription] =
    ref.update(_ + (sub.id -> sub)).as(sub)

object InMemorySubscriptionRepository:
  val layer: ULayer[SubscriptionRepository] = ZLayer {
    Ref.make(Map.empty[SubscriptionId, Subscription]).map(InMemorySubscriptionRepository(_))
  }

final case class InMemoryPackageDefinitionRepository(ref: Ref[Map[PackageDefinitionId, PackageDefinition]])
    extends PackageDefinitionRepository:
  def create(pd: PackageDefinition): IO[DomainError, PackageDefinition] =
    ref.update(_ + (pd.id -> pd)).as(pd)

  def findById(id: PackageDefinitionId): IO[DomainError, Option[PackageDefinition]] =
    ref.get.map(_.get(id))

  def findAll: IO[DomainError, List[PackageDefinition]] =
    ref.get.map(_.values.toList)

object InMemoryPackageDefinitionRepository:
  val layer: ULayer[PackageDefinitionRepository] = ZLayer {
    Ref.make(Map.empty[PackageDefinitionId, PackageDefinition]).map(InMemoryPackageDefinitionRepository(_))
  }

final case class InMemoryCreditPackageRepository(ref: Ref[Map[CreditPackageId, CreditPackage]])
    extends CreditPackageRepository:
  def create(cp: CreditPackage): IO[DomainError, CreditPackage] =
    ref.update(_ + (cp.id -> cp)).as(cp)

  def findById(id: CreditPackageId): IO[DomainError, Option[CreditPackage]] =
    ref.get.map(_.get(id))

  def findActiveByUser(userId: UserId): IO[DomainError, List[CreditPackage]] =
    ref.get.map(_.values.filter(p =>
      p.userId == userId && p.status == CreditPackageStatus.Active
    ).toList)

  def update(cp: CreditPackage): IO[DomainError, CreditPackage] =
    ref.update(_ + (cp.id -> cp)).as(cp)

object InMemoryCreditPackageRepository:
  val layer: ULayer[CreditPackageRepository] = ZLayer {
    Ref.make(Map.empty[CreditPackageId, CreditPackage]).map(InMemoryCreditPackageRepository(_))
  }
