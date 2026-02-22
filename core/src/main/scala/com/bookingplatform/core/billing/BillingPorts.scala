package com.bookingplatform.core.billing

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import zio.*

trait SubscriptionPlanRepository:
  def create(plan: SubscriptionPlan): IO[DomainError, SubscriptionPlan]
  def findById(id: SubscriptionPlanId): IO[DomainError, Option[SubscriptionPlan]]
  def findAll: IO[DomainError, List[SubscriptionPlan]]

trait SubscriptionRepository:
  def create(subscription: Subscription): IO[DomainError, Subscription]
  def findById(id: SubscriptionId): IO[DomainError, Option[Subscription]]
  def findByUser(userId: UserId): IO[DomainError, List[Subscription]]
  def findActiveByUser(userId: UserId): IO[DomainError, Option[Subscription]]
  def update(subscription: Subscription): IO[DomainError, Subscription]

trait PackageDefinitionRepository:
  def create(packageDef: PackageDefinition): IO[DomainError, PackageDefinition]
  def findById(id: PackageDefinitionId): IO[DomainError, Option[PackageDefinition]]
  def findAll: IO[DomainError, List[PackageDefinition]]

trait CreditPackageRepository:
  def create(creditPkg: CreditPackage): IO[DomainError, CreditPackage]
  def findById(id: CreditPackageId): IO[DomainError, Option[CreditPackage]]
  def findActiveByUser(userId: UserId): IO[DomainError, List[CreditPackage]]
  def update(creditPkg: CreditPackage): IO[DomainError, CreditPackage]

trait PaymentGateway:
  def charge(userId: UserId, amount: Money, description: String): IO[DomainError, String]
  def refund(chargeId: String, amount: Money): IO[DomainError, Unit]
