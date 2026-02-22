package com.bookingplatform.shared.ids

import zio.json.*

import java.util.UUID

// Typed ID wrappers for type-safe entity references
opaque type UserId = UUID
object UserId:
  def apply(value: UUID): UserId        = value
  def generate: UserId                   = UUID.randomUUID()
  def fromString(s: String): UserId      = UUID.fromString(s)
  extension (id: UserId) def value: UUID = id

  given JsonEncoder[UserId] = JsonEncoder[UUID]
  given JsonDecoder[UserId] = JsonDecoder[UUID]

opaque type VenueId = UUID
object VenueId:
  def apply(value: UUID): VenueId        = value
  def generate: VenueId                   = UUID.randomUUID()
  def fromString(s: String): VenueId      = UUID.fromString(s)
  extension (id: VenueId) def value: UUID = id

  given JsonEncoder[VenueId] = JsonEncoder[UUID]
  given JsonDecoder[VenueId] = JsonDecoder[UUID]

opaque type RoomId = UUID
object RoomId:
  def apply(value: UUID): RoomId        = value
  def generate: RoomId                   = UUID.randomUUID()
  def fromString(s: String): RoomId      = UUID.fromString(s)
  extension (id: RoomId) def value: UUID = id

  given JsonEncoder[RoomId] = JsonEncoder[UUID]
  given JsonDecoder[RoomId] = JsonDecoder[UUID]

opaque type StationId = UUID
object StationId:
  def apply(value: UUID): StationId        = value
  def generate: StationId                   = UUID.randomUUID()
  def fromString(s: String): StationId      = UUID.fromString(s)
  extension (id: StationId) def value: UUID = id

  given JsonEncoder[StationId] = JsonEncoder[UUID]
  given JsonDecoder[StationId] = JsonDecoder[UUID]

opaque type ServiceDefinitionId = UUID
object ServiceDefinitionId:
  def apply(value: UUID): ServiceDefinitionId        = value
  def generate: ServiceDefinitionId                   = UUID.randomUUID()
  def fromString(s: String): ServiceDefinitionId      = UUID.fromString(s)
  extension (id: ServiceDefinitionId) def value: UUID = id

  given JsonEncoder[ServiceDefinitionId] = JsonEncoder[UUID]
  given JsonDecoder[ServiceDefinitionId] = JsonDecoder[UUID]

opaque type ClassDefinitionId = UUID
object ClassDefinitionId:
  def apply(value: UUID): ClassDefinitionId        = value
  def generate: ClassDefinitionId                   = UUID.randomUUID()
  def fromString(s: String): ClassDefinitionId      = UUID.fromString(s)
  extension (id: ClassDefinitionId) def value: UUID = id

  given JsonEncoder[ClassDefinitionId] = JsonEncoder[UUID]
  given JsonDecoder[ClassDefinitionId] = JsonDecoder[UUID]

opaque type ClassInstanceId = UUID
object ClassInstanceId:
  def apply(value: UUID): ClassInstanceId        = value
  def generate: ClassInstanceId                   = UUID.randomUUID()
  def fromString(s: String): ClassInstanceId      = UUID.fromString(s)
  extension (id: ClassInstanceId) def value: UUID = id

  given JsonEncoder[ClassInstanceId] = JsonEncoder[UUID]
  given JsonDecoder[ClassInstanceId] = JsonDecoder[UUID]

opaque type BookingId = UUID
object BookingId:
  def apply(value: UUID): BookingId        = value
  def generate: BookingId                   = UUID.randomUUID()
  def fromString(s: String): BookingId      = UUID.fromString(s)
  extension (id: BookingId) def value: UUID = id

  given JsonEncoder[BookingId] = JsonEncoder[UUID]
  given JsonDecoder[BookingId] = JsonDecoder[UUID]

opaque type WaitlistEntryId = UUID
object WaitlistEntryId:
  def apply(value: UUID): WaitlistEntryId        = value
  def generate: WaitlistEntryId                   = UUID.randomUUID()
  def fromString(s: String): WaitlistEntryId      = UUID.fromString(s)
  extension (id: WaitlistEntryId) def value: UUID = id

  given JsonEncoder[WaitlistEntryId] = JsonEncoder[UUID]
  given JsonDecoder[WaitlistEntryId] = JsonDecoder[UUID]

opaque type SubscriptionPlanId = UUID
object SubscriptionPlanId:
  def apply(value: UUID): SubscriptionPlanId        = value
  def generate: SubscriptionPlanId                   = UUID.randomUUID()
  def fromString(s: String): SubscriptionPlanId      = UUID.fromString(s)
  extension (id: SubscriptionPlanId) def value: UUID = id

  given JsonEncoder[SubscriptionPlanId] = JsonEncoder[UUID]
  given JsonDecoder[SubscriptionPlanId] = JsonDecoder[UUID]

opaque type SubscriptionId = UUID
object SubscriptionId:
  def apply(value: UUID): SubscriptionId        = value
  def generate: SubscriptionId                   = UUID.randomUUID()
  def fromString(s: String): SubscriptionId      = UUID.fromString(s)
  extension (id: SubscriptionId) def value: UUID = id

  given JsonEncoder[SubscriptionId] = JsonEncoder[UUID]
  given JsonDecoder[SubscriptionId] = JsonDecoder[UUID]

opaque type PackageDefinitionId = UUID
object PackageDefinitionId:
  def apply(value: UUID): PackageDefinitionId        = value
  def generate: PackageDefinitionId                   = UUID.randomUUID()
  def fromString(s: String): PackageDefinitionId      = UUID.fromString(s)
  extension (id: PackageDefinitionId) def value: UUID = id

  given JsonEncoder[PackageDefinitionId] = JsonEncoder[UUID]
  given JsonDecoder[PackageDefinitionId] = JsonDecoder[UUID]

opaque type CreditPackageId = UUID
object CreditPackageId:
  def apply(value: UUID): CreditPackageId        = value
  def generate: CreditPackageId                   = UUID.randomUUID()
  def fromString(s: String): CreditPackageId      = UUID.fromString(s)
  extension (id: CreditPackageId) def value: UUID = id

  given JsonEncoder[CreditPackageId] = JsonEncoder[UUID]
  given JsonDecoder[CreditPackageId] = JsonDecoder[UUID]

opaque type CancellationPolicyId = UUID
object CancellationPolicyId:
  def apply(value: UUID): CancellationPolicyId        = value
  def generate: CancellationPolicyId                   = UUID.randomUUID()
  def fromString(s: String): CancellationPolicyId      = UUID.fromString(s)
  extension (id: CancellationPolicyId) def value: UUID = id

  given JsonEncoder[CancellationPolicyId] = JsonEncoder[UUID]
  given JsonDecoder[CancellationPolicyId] = JsonDecoder[UUID]

opaque type WeeklyScheduleId = UUID
object WeeklyScheduleId:
  def apply(value: UUID): WeeklyScheduleId        = value
  def generate: WeeklyScheduleId                   = UUID.randomUUID()
  def fromString(s: String): WeeklyScheduleId      = UUID.fromString(s)
  extension (id: WeeklyScheduleId) def value: UUID = id

  given JsonEncoder[WeeklyScheduleId] = JsonEncoder[UUID]
  given JsonDecoder[WeeklyScheduleId] = JsonDecoder[UUID]

opaque type DomainEventId = UUID
object DomainEventId:
  def apply(value: UUID): DomainEventId        = value
  def generate: DomainEventId                   = UUID.randomUUID()
  def fromString(s: String): DomainEventId      = UUID.fromString(s)
  extension (id: DomainEventId) def value: UUID = id

  given JsonEncoder[DomainEventId] = JsonEncoder[UUID]
  given JsonDecoder[DomainEventId] = JsonDecoder[UUID]
