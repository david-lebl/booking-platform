package com.bookingplatform.core.billing

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*
import zio.*

import java.time.{Duration, Instant}

final case class BillingService(
    subscriptionPlanRepo: SubscriptionPlanRepository,
    subscriptionRepo: SubscriptionRepository,
    packageDefRepo: PackageDefinitionRepository,
    creditPackageRepo: CreditPackageRepository,
    paymentGateway: PaymentGateway
):

  // --- Subscription Plans ---

  def createSubscriptionPlan(
      name: NonEmptyString,
      price: Money,
      interval: BillingInterval,
      includedBookings: Option[Int],
      includedCredits: Option[Int],
      allowedServiceTypes: List[ServiceCategory]
  ): IO[DomainError, SubscriptionPlan] =
    val plan = SubscriptionPlan(
      id = SubscriptionPlanId.generate,
      name = name,
      price = price,
      interval = interval,
      includedBookings = includedBookings,
      includedCredits = includedCredits,
      allowedServiceTypes = allowedServiceTypes,
      createdAt = Instant.now()
    )
    subscriptionPlanRepo.create(plan)

  def listSubscriptionPlans: IO[DomainError, List[SubscriptionPlan]] =
    subscriptionPlanRepo.findAll

  // --- Subscriptions ---

  def subscribe(userId: UserId, planId: SubscriptionPlanId): IO[DomainError, Subscription] =
    for
      plan <- subscriptionPlanRepo.findById(planId).flatMap {
        case Some(p) => ZIO.succeed(p)
        case None    => ZIO.fail(DomainError.notFound("SubscriptionPlan", planId.value))
      }
      existing <- subscriptionRepo.findActiveByUser(userId)
      _ <- ZIO.when(existing.isDefined)(
        ZIO.fail(DomainError.ConflictError("User already has an active subscription"))
      )
      chargeId <- paymentGateway.charge(userId, plan.price, s"Subscription: ${NonEmptyString.unwrap(plan.name)}")
      now       = Instant.now()
      periodEnd = intervalEnd(now, plan.interval)
      subscription = Subscription(
        id = SubscriptionId.generate,
        userId = userId,
        planId = planId,
        status = SubscriptionStatus.Active,
        currentPeriodStart = now,
        currentPeriodEnd = periodEnd,
        pausedAt = None,
        resumesAt = None,
        createdAt = now
      )
      created <- subscriptionRepo.create(subscription)
    yield created

  def cancelSubscription(subscriptionId: SubscriptionId): IO[DomainError, Subscription] =
    for
      sub <- subscriptionRepo.findById(subscriptionId).flatMap {
        case Some(s) => ZIO.succeed(s)
        case None    => ZIO.fail(DomainError.notFound("Subscription", subscriptionId.value))
      }
      _ <- ZIO.when(sub.status != SubscriptionStatus.Active && sub.status != SubscriptionStatus.Paused)(
        ZIO.fail(DomainError.SubscriptionNotActive(subscriptionId.value))
      )
      updated = sub.copy(status = SubscriptionStatus.Cancelled)
      result <- subscriptionRepo.update(updated)
    yield result

  def pauseSubscription(subscriptionId: SubscriptionId): IO[DomainError, Subscription] =
    for
      sub <- subscriptionRepo.findById(subscriptionId).flatMap {
        case Some(s) => ZIO.succeed(s)
        case None    => ZIO.fail(DomainError.notFound("Subscription", subscriptionId.value))
      }
      _ <- ZIO.when(sub.status != SubscriptionStatus.Active)(
        ZIO.fail(DomainError.SubscriptionNotActive(subscriptionId.value))
      )
      now = Instant.now()
      updated = sub.copy(status = SubscriptionStatus.Paused, pausedAt = Some(now))
      result <- subscriptionRepo.update(updated)
    yield result

  // --- Package Definitions ---

  def createPackageDefinition(
      name: NonEmptyString,
      credits: PositiveInt,
      price: Money,
      validityDays: PositiveInt,
      allowedServiceTypes: List[ServiceCategory]
  ): IO[DomainError, PackageDefinition] =
    val pkgDef = PackageDefinition(
      id = PackageDefinitionId.generate,
      name = name,
      credits = credits,
      price = price,
      validityDays = validityDays,
      allowedServiceTypes = allowedServiceTypes,
      createdAt = Instant.now()
    )
    packageDefRepo.create(pkgDef)

  def listPackageDefinitions: IO[DomainError, List[PackageDefinition]] =
    packageDefRepo.findAll

  // --- Credit Packages ---

  def purchaseCreditPackage(userId: UserId, packageDefId: PackageDefinitionId): IO[DomainError, CreditPackage] =
    for
      pkgDef <- packageDefRepo.findById(packageDefId).flatMap {
        case Some(p) => ZIO.succeed(p)
        case None    => ZIO.fail(DomainError.notFound("PackageDefinition", packageDefId.value))
      }
      _ <- paymentGateway.charge(userId, pkgDef.price, s"Credit Package: ${NonEmptyString.unwrap(pkgDef.name)}")
      now = Instant.now()
      creditPkg = CreditPackage(
        id = CreditPackageId.generate,
        userId = userId,
        packageDefinitionId = packageDefId,
        totalCredits = PositiveInt.unwrap(pkgDef.credits),
        remainingCredits = PositiveInt.unwrap(pkgDef.credits),
        expiresAt = now.plus(Duration.ofDays(PositiveInt.unwrap(pkgDef.validityDays).toLong)),
        status = CreditPackageStatus.Active,
        createdAt = now
      )
      created <- creditPackageRepo.create(creditPkg)
    yield created

  def deductCredit(userId: UserId): IO[DomainError, CreditPackage] =
    for
      packages <- creditPackageRepo.findActiveByUser(userId)
      now       = Instant.now()
      activePkg <- ZIO.fromOption(
        packages
          .filter(p => p.hasCredits && !p.isExpired(now))
          .sortBy(_.expiresAt)
          .headOption
      ).mapError(_ => DomainError.InsufficientCredits(1, 0))
      deducted <- ZIO.fromEither(activePkg.deductCredit).mapError(msg => DomainError.InsufficientCredits(1, 0))
      result   <- creditPackageRepo.update(deducted)
    yield result

  def getUserActivePackages(userId: UserId): IO[DomainError, List[CreditPackage]] =
    creditPackageRepo.findActiveByUser(userId)

  private def intervalEnd(start: Instant, interval: BillingInterval): Instant = interval match
    case BillingInterval.Monthly   => start.plus(Duration.ofDays(30))
    case BillingInterval.Quarterly => start.plus(Duration.ofDays(90))
    case BillingInterval.Yearly    => start.plus(Duration.ofDays(365))

object BillingService:
  val layer: URLayer[
    SubscriptionPlanRepository & SubscriptionRepository & PackageDefinitionRepository & CreditPackageRepository & PaymentGateway,
    BillingService
  ] = ZLayer.fromFunction(BillingService.apply)
