package com.bookingplatform.core.billing

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*

import java.time.Instant

final case class SubscriptionPlan(
    id: SubscriptionPlanId,
    name: NonEmptyString,
    price: Money,
    interval: BillingInterval,
    includedBookings: Option[Int],
    includedCredits: Option[Int],
    allowedServiceTypes: List[ServiceCategory],
    createdAt: Instant
)

final case class Subscription(
    id: SubscriptionId,
    userId: UserId,
    planId: SubscriptionPlanId,
    status: SubscriptionStatus,
    currentPeriodStart: Instant,
    currentPeriodEnd: Instant,
    pausedAt: Option[Instant],
    resumesAt: Option[Instant],
    createdAt: Instant
)

final case class PackageDefinition(
    id: PackageDefinitionId,
    name: NonEmptyString,
    credits: PositiveInt,
    price: Money,
    validityDays: PositiveInt,
    allowedServiceTypes: List[ServiceCategory],
    createdAt: Instant
)

final case class CreditPackage(
    id: CreditPackageId,
    userId: UserId,
    packageDefinitionId: PackageDefinitionId,
    totalCredits: Int,
    remainingCredits: Int,
    expiresAt: Instant,
    status: CreditPackageStatus,
    createdAt: Instant
):
  def hasCredits: Boolean        = remainingCredits > 0
  def isExpired(now: Instant): Boolean = now.isAfter(expiresAt)
  def deductCredit: Either[String, CreditPackage] =
    if remainingCredits <= 0 then Left("No credits remaining")
    else Right(copy(remainingCredits = remainingCredits - 1))
