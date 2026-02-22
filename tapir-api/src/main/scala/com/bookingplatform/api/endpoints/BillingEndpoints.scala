package com.bookingplatform.api.endpoints

import com.bookingplatform.api.codecs.TapirCodecs.given
import com.bookingplatform.api.errors.ApiError
import com.bookingplatform.shared.models.*
import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import java.util.UUID

object BillingEndpoints:

  // --- Subscription Plans ---

  val createSubscriptionPlan: Endpoint[Unit, CreateSubscriptionPlanRequest, ApiError, SubscriptionPlanResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("subscription-plans")
      .in(jsonBody[CreateSubscriptionPlanRequest])
      .out(jsonBody[SubscriptionPlanResponse])
      .tag("Billing")

  val listSubscriptionPlans: Endpoint[Unit, Unit, ApiError, List[SubscriptionPlanResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("subscription-plans")
      .out(jsonBody[List[SubscriptionPlanResponse]])
      .tag("Billing")

  // --- Subscriptions ---

  val subscribe: Endpoint[String, CreateSubscriptionRequest, ApiError, SubscriptionResponse, Any] =
    BaseEndpoint.securedEndpoint.post
      .in("subscriptions")
      .in(jsonBody[CreateSubscriptionRequest])
      .out(jsonBody[SubscriptionResponse])
      .tag("Billing")

  val cancelSubscription: Endpoint[String, UUID, ApiError, SubscriptionResponse, Any] =
    BaseEndpoint.securedEndpoint.post
      .in("subscriptions" / path[UUID]("subscriptionId") / "cancel")
      .out(jsonBody[SubscriptionResponse])
      .tag("Billing")

  // --- Package Definitions ---

  val createPackageDefinition: Endpoint[Unit, CreatePackageDefinitionRequest, ApiError, PackageDefinitionResponse, Any] =
    BaseEndpoint.baseEndpoint.post
      .in("package-definitions")
      .in(jsonBody[CreatePackageDefinitionRequest])
      .out(jsonBody[PackageDefinitionResponse])
      .tag("Billing")

  val listPackageDefinitions: Endpoint[Unit, Unit, ApiError, List[PackageDefinitionResponse], Any] =
    BaseEndpoint.baseEndpoint.get
      .in("package-definitions")
      .out(jsonBody[List[PackageDefinitionResponse]])
      .tag("Billing")

  // --- Credit Packages ---

  val purchaseCreditPackage: Endpoint[String, PurchaseCreditPackageRequest, ApiError, CreditPackageResponse, Any] =
    BaseEndpoint.securedEndpoint.post
      .in("credit-packages")
      .in(jsonBody[PurchaseCreditPackageRequest])
      .out(jsonBody[CreditPackageResponse])
      .tag("Billing")

  val listUserCreditPackages: Endpoint[String, Unit, ApiError, List[CreditPackageResponse], Any] =
    BaseEndpoint.securedEndpoint.get
      .in("credit-packages" / "my")
      .out(jsonBody[List[CreditPackageResponse]])
      .tag("Billing")

  // --- Health ---

  val health: Endpoint[Unit, Unit, ApiError, HealthResponse, Any] =
    BaseEndpoint.baseEndpoint.get
      .in("health")
      .out(jsonBody[HealthResponse])
      .tag("System")
