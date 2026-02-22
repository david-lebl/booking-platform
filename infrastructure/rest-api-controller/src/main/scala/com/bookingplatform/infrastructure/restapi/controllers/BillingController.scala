package com.bookingplatform.infrastructure.restapi.controllers

import com.bookingplatform.api.endpoints.BillingEndpoints
import com.bookingplatform.api.errors.{ApiError, ErrorMapping}
import com.bookingplatform.core.billing.BillingService
import com.bookingplatform.core.common.*
import com.bookingplatform.core.identity.AuthService
import com.bookingplatform.infrastructure.restapi.controllers.ValidationHelper.validate
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*
import sttp.tapir.ztapir.*
import zio.*

import java.time.Instant

object BillingController:

  def endpoints(billingService: BillingService, authService: AuthService) = List(
    createSubscriptionPlan(billingService),
    listSubscriptionPlans(billingService),
    subscribe(billingService, authService),
    cancelSubscription(billingService, authService),
    createPackageDefinition(billingService),
    listPackageDefinitions(billingService),
    purchaseCreditPackage(billingService, authService),
    listUserCreditPackages(billingService, authService),
    health
  )

  private def createSubscriptionPlan(svc: BillingService) =
    BillingEndpoints.createSubscriptionPlan.zServerLogic { req =>
      (for
        name     <- validate(NonEmptyString.make(req.name))
        currency <- ZIO.fromEither(Currency.values.find(_.toString == req.priceCurrency)
          .toRight(DomainError.ValidationError(s"Invalid currency: ${req.priceCurrency}")))
        plan     <- svc.createSubscriptionPlan(name, Money(req.priceAmount, currency), req.interval,
          req.includedBookings, req.includedCredits, req.allowedServiceTypes)
      yield SubscriptionPlanResponse(plan.id.value, NonEmptyString.unwrap(plan.name), plan.price.amount,
        plan.price.currency.toString, plan.interval, plan.includedBookings, plan.includedCredits,
        plan.allowedServiceTypes, plan.createdAt)
      ).mapError(ErrorMapping.toApiError)
    }

  private def listSubscriptionPlans(svc: BillingService) =
    BillingEndpoints.listSubscriptionPlans.zServerLogic { _ =>
      svc.listSubscriptionPlans.map(_.map(p =>
        SubscriptionPlanResponse(p.id.value, NonEmptyString.unwrap(p.name), p.price.amount,
          p.price.currency.toString, p.interval, p.includedBookings, p.includedCredits,
          p.allowedServiceTypes, p.createdAt)
      )).mapError(ErrorMapping.toApiError)
    }

  private def subscribe(svc: BillingService, auth: AuthService) =
    BillingEndpoints.subscribe
      .zServerSecurityLogic(token => auth.authenticate(token).mapError(ErrorMapping.toApiError))
      .serverLogic { userId => req =>
        svc.subscribe(userId, SubscriptionPlanId(req.planId)).map(s =>
          SubscriptionResponse(s.id.value, s.userId.value, s.planId.value, s.status,
            s.currentPeriodStart, s.currentPeriodEnd, s.pausedAt, s.resumesAt, s.createdAt)
        ).mapError(ErrorMapping.toApiError)
      }

  private def cancelSubscription(svc: BillingService, auth: AuthService) =
    BillingEndpoints.cancelSubscription
      .zServerSecurityLogic(token => auth.authenticate(token).mapError(ErrorMapping.toApiError))
      .serverLogic { _ => subscriptionId =>
        svc.cancelSubscription(SubscriptionId(subscriptionId)).map(s =>
          SubscriptionResponse(s.id.value, s.userId.value, s.planId.value, s.status,
            s.currentPeriodStart, s.currentPeriodEnd, s.pausedAt, s.resumesAt, s.createdAt)
        ).mapError(ErrorMapping.toApiError)
      }

  private def createPackageDefinition(svc: BillingService) =
    BillingEndpoints.createPackageDefinition.zServerLogic { req =>
      (for
        name     <- validate(NonEmptyString.make(req.name))
        credits  <- validate(PositiveInt.make(req.credits))
        validity <- validate(PositiveInt.make(req.validityDays))
        currency <- ZIO.fromEither(Currency.values.find(_.toString == req.priceCurrency)
          .toRight(DomainError.ValidationError(s"Invalid currency: ${req.priceCurrency}")))
        pd <- svc.createPackageDefinition(name, credits, Money(req.priceAmount, currency),
          validity, req.allowedServiceTypes)
      yield PackageDefinitionResponse(pd.id.value, NonEmptyString.unwrap(pd.name),
        PositiveInt.unwrap(pd.credits), pd.price.amount, pd.price.currency.toString,
        PositiveInt.unwrap(pd.validityDays), pd.allowedServiceTypes, pd.createdAt)
      ).mapError(ErrorMapping.toApiError)
    }

  private def listPackageDefinitions(svc: BillingService) =
    BillingEndpoints.listPackageDefinitions.zServerLogic { _ =>
      svc.listPackageDefinitions.map(_.map(pd =>
        PackageDefinitionResponse(pd.id.value, NonEmptyString.unwrap(pd.name),
          PositiveInt.unwrap(pd.credits), pd.price.amount, pd.price.currency.toString,
          PositiveInt.unwrap(pd.validityDays), pd.allowedServiceTypes, pd.createdAt)
      )).mapError(ErrorMapping.toApiError)
    }

  private def purchaseCreditPackage(svc: BillingService, auth: AuthService) =
    BillingEndpoints.purchaseCreditPackage
      .zServerSecurityLogic(token => auth.authenticate(token).mapError(ErrorMapping.toApiError))
      .serverLogic { userId => req =>
        svc.purchaseCreditPackage(userId, PackageDefinitionId(req.packageDefinitionId)).map(cp =>
          CreditPackageResponse(cp.id.value, cp.userId.value, cp.packageDefinitionId.value,
            cp.totalCredits, cp.remainingCredits, cp.expiresAt, cp.status, cp.createdAt)
        ).mapError(ErrorMapping.toApiError)
      }

  private def listUserCreditPackages(svc: BillingService, auth: AuthService) =
    BillingEndpoints.listUserCreditPackages
      .zServerSecurityLogic(token => auth.authenticate(token).mapError(ErrorMapping.toApiError))
      .serverLogic { userId => _ =>
        svc.getUserActivePackages(userId).map(_.map(cp =>
          CreditPackageResponse(cp.id.value, cp.userId.value, cp.packageDefinitionId.value,
            cp.totalCredits, cp.remainingCredits, cp.expiresAt, cp.status, cp.createdAt)
        )).mapError(ErrorMapping.toApiError)
      }

  private def health =
    BillingEndpoints.health.zServerLogic { _ =>
      ZIO.succeed(HealthResponse("ok", "0.1.0-SNAPSHOT", Instant.now()))
    }
