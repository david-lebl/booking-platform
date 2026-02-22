package com.bookingplatform.shared.models

import zio.json.*

import java.time.{Instant, LocalDate, LocalTime, DayOfWeek}
import java.util.UUID

// --- Venue ---

final case class CreateVenueRequest(
    name: String,
    address: String,
    timezone: String
) derives JsonEncoder,
      JsonDecoder

final case class UpdateVenueRequest(
    name: Option[String],
    address: Option[String],
    timezone: Option[String],
    status: Option[VenueStatus]
) derives JsonEncoder,
      JsonDecoder

final case class VenueResponse(
    id: UUID,
    name: String,
    address: String,
    timezone: String,
    status: VenueStatus,
    createdAt: Instant,
    updatedAt: Instant
) derives JsonEncoder,
      JsonDecoder

// --- Room ---

final case class CreateRoomRequest(
    venueId: UUID,
    name: String,
    capacity: Int,
    roomType: RoomType,
    stations: Option[Int]
) derives JsonEncoder,
      JsonDecoder

final case class RoomResponse(
    id: UUID,
    venueId: UUID,
    name: String,
    capacity: Int,
    roomType: RoomType,
    stations: Int,
    createdAt: Instant
) derives JsonEncoder,
      JsonDecoder

// --- Station ---

final case class CreateStationRequest(
    roomId: UUID,
    name: String,
    stationType: StationType
) derives JsonEncoder,
      JsonDecoder

final case class StationResponse(
    id: UUID,
    roomId: UUID,
    name: String,
    stationType: StationType,
    status: StationStatus
) derives JsonEncoder,
      JsonDecoder

// --- Service Definition ---

final case class CreateServiceDefinitionRequest(
    name: String,
    category: ServiceCategory,
    durationMinutes: Int,
    capacity: Int,
    requiresStation: Boolean,
    priceAmount: BigDecimal,
    priceCurrency: String
) derives JsonEncoder,
      JsonDecoder

final case class ServiceDefinitionResponse(
    id: UUID,
    name: String,
    category: ServiceCategory,
    durationMinutes: Int,
    capacity: Int,
    requiresStation: Boolean,
    priceAmount: BigDecimal,
    priceCurrency: String,
    createdAt: Instant
) derives JsonEncoder,
      JsonDecoder

// --- User ---

final case class CreateUserRequest(
    email: String,
    firstName: String,
    lastName: String,
    phone: Option[String],
    role: UserRole
) derives JsonEncoder,
      JsonDecoder

final case class UpdateUserRequest(
    firstName: Option[String],
    lastName: Option[String],
    phone: Option[String],
    status: Option[UserStatus]
) derives JsonEncoder,
      JsonDecoder

final case class UserResponse(
    id: UUID,
    email: String,
    firstName: String,
    lastName: String,
    phone: Option[String],
    role: UserRole,
    status: UserStatus,
    createdAt: Instant,
    updatedAt: Instant
) derives JsonEncoder,
      JsonDecoder

// --- Class Definition ---

final case class CreateClassDefinitionRequest(
    serviceDefinitionId: UUID,
    venueId: UUID,
    roomId: UUID,
    instructorId: UUID,
    name: String,
    capacity: Int,
    durationMinutes: Int,
    bookingWindowMinAdvanceMinutes: Int,
    bookingWindowMaxAdvanceDays: Int,
    cancellationPolicyId: Option[UUID]
) derives JsonEncoder,
      JsonDecoder

final case class ClassDefinitionResponse(
    id: UUID,
    serviceDefinitionId: UUID,
    venueId: UUID,
    roomId: UUID,
    instructorId: UUID,
    name: String,
    capacity: Int,
    durationMinutes: Int,
    bookingWindowMinAdvanceMinutes: Int,
    bookingWindowMaxAdvanceDays: Int,
    cancellationPolicyId: Option[UUID],
    createdAt: Instant
) derives JsonEncoder,
      JsonDecoder

// --- Class Instance ---

final case class ClassInstanceResponse(
    id: UUID,
    classDefinitionId: UUID,
    startTime: Instant,
    endTime: Instant,
    currentBookings: Int,
    capacity: Int,
    status: ClassInstanceStatus
) derives JsonEncoder,
      JsonDecoder

// --- Weekly Schedule ---

final case class CreateWeeklyScheduleRequest(
    classDefinitionId: UUID,
    dayOfWeek: String,
    startTime: String,
    effectiveFrom: LocalDate,
    effectiveUntil: Option[LocalDate]
) derives JsonEncoder,
      JsonDecoder

final case class WeeklyScheduleResponse(
    id: UUID,
    classDefinitionId: UUID,
    dayOfWeek: String,
    startTime: String,
    effectiveFrom: LocalDate,
    effectiveUntil: Option[LocalDate]
) derives JsonEncoder,
      JsonDecoder

// --- Booking ---

final case class CreateBookingRequest(
    classInstanceId: UUID,
    stationId: Option[UUID]
) derives JsonEncoder,
      JsonDecoder

final case class BookingResponse(
    id: UUID,
    userId: UUID,
    classInstanceId: UUID,
    stationId: Option[UUID],
    status: BookingStatus,
    cancellationFee: Option[BigDecimal],
    createdAt: Instant,
    updatedAt: Instant
) derives JsonEncoder,
      JsonDecoder

// --- Waitlist ---

final case class WaitlistEntryResponse(
    id: UUID,
    classInstanceId: UUID,
    userId: UUID,
    position: Int,
    status: WaitlistStatus,
    offerExpiresAt: Option[Instant],
    createdAt: Instant
) derives JsonEncoder,
      JsonDecoder

// --- Subscription ---

final case class CreateSubscriptionPlanRequest(
    name: String,
    priceAmount: BigDecimal,
    priceCurrency: String,
    interval: BillingInterval,
    includedBookings: Option[Int],
    includedCredits: Option[Int],
    allowedServiceTypes: List[ServiceCategory]
) derives JsonEncoder,
      JsonDecoder

final case class SubscriptionPlanResponse(
    id: UUID,
    name: String,
    priceAmount: BigDecimal,
    priceCurrency: String,
    interval: BillingInterval,
    includedBookings: Option[Int],
    includedCredits: Option[Int],
    allowedServiceTypes: List[ServiceCategory],
    createdAt: Instant
) derives JsonEncoder,
      JsonDecoder

final case class CreateSubscriptionRequest(
    planId: UUID
) derives JsonEncoder,
      JsonDecoder

final case class SubscriptionResponse(
    id: UUID,
    userId: UUID,
    planId: UUID,
    status: SubscriptionStatus,
    currentPeriodStart: Instant,
    currentPeriodEnd: Instant,
    pausedAt: Option[Instant],
    resumesAt: Option[Instant],
    createdAt: Instant
) derives JsonEncoder,
      JsonDecoder

// --- Credit Package ---

final case class CreatePackageDefinitionRequest(
    name: String,
    credits: Int,
    priceAmount: BigDecimal,
    priceCurrency: String,
    validityDays: Int,
    allowedServiceTypes: List[ServiceCategory]
) derives JsonEncoder,
      JsonDecoder

final case class PackageDefinitionResponse(
    id: UUID,
    name: String,
    credits: Int,
    priceAmount: BigDecimal,
    priceCurrency: String,
    validityDays: Int,
    allowedServiceTypes: List[ServiceCategory],
    createdAt: Instant
) derives JsonEncoder,
      JsonDecoder

final case class PurchaseCreditPackageRequest(
    packageDefinitionId: UUID
) derives JsonEncoder,
      JsonDecoder

final case class CreditPackageResponse(
    id: UUID,
    userId: UUID,
    packageDefinitionId: UUID,
    totalCredits: Int,
    remainingCredits: Int,
    expiresAt: Instant,
    status: CreditPackageStatus,
    createdAt: Instant
) derives JsonEncoder,
      JsonDecoder

// --- Cancellation Policy ---

final case class CancellationRuleDto(
    noticePeriodMinutes: Int,
    feeType: CancellationFeeType,
    feeValue: Option[BigDecimal]
) derives JsonEncoder,
      JsonDecoder

final case class CreateCancellationPolicyRequest(
    name: String,
    rules: List[CancellationRuleDto]
) derives JsonEncoder,
      JsonDecoder

final case class CancellationPolicyResponse(
    id: UUID,
    name: String,
    rules: List[CancellationRuleDto],
    createdAt: Instant
) derives JsonEncoder,
      JsonDecoder

// --- Generate Instances ---

final case class GenerateInstancesRequest(
    classDefinitionId: UUID,
    fromDate: LocalDate,
    toDate: LocalDate
) derives JsonEncoder,
      JsonDecoder

// --- Health ---

final case class HealthResponse(
    status: String,
    version: String,
    timestamp: Instant
) derives JsonEncoder,
      JsonDecoder
