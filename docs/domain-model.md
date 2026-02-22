# Domain Model

## Entity Relationship Overview

```
User ──< Booking >── ClassInstance ──> ClassDefinition ──> ServiceDefinition
  │         │              │                │
  │         └── Station?   │                ├── Venue
  │                        │                ├── Room
  │    WaitlistEntry >─────┘                ├── Instructor (User)
  │                                         └── CancellationPolicy ──< CancellationRule
  │
  ├──< Subscription >── SubscriptionPlan
  └──< CreditPackage >── PackageDefinition
```

## Value Objects

Defined in `core/common/ValueObjects.scala` using ZIO Prelude Newtypes:

| Type | Underlying | Validation |
|------|-----------|------------|
| `Email` | String | Regex: `^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$` |
| `NonEmptyString` | String | Length > 0 |
| `PositiveInt` | Int | > 0 |
| `NonNegativeInt` | Int | >= 0 |
| `Phone` | String | Regex: `^\\+?[1-9]\\d{1,14}$` (E.164) |
| `Timezone` | String | Non-empty |
| `Money` | (BigDecimal, Currency) | Currency-safe arithmetic |
| `Currency` | Enum | CZK, EUR, USD, GBP |

## Typed IDs

All entity IDs are opaque types wrapping `UUID` (defined in `shared/ids/Ids.scala`):

UserId, VenueId, RoomId, StationId, ServiceDefinitionId, ClassDefinitionId, ClassInstanceId, BookingId, WaitlistEntryId, SubscriptionPlanId, SubscriptionId, PackageDefinitionId, CreditPackageId, CancellationPolicyId, WeeklyScheduleId, DomainEventId

## Entities

### User

```
User
├── id: UserId
├── email: Email (unique)
├── firstName: NonEmptyString
├── lastName: NonEmptyString
├── phone: Option[Phone]
├── role: UserRole (Client | Trainer | Admin | SuperAdmin)
├── status: UserStatus (Active | Inactive | Suspended)
├── createdAt: Instant
└── updatedAt: Instant
```

**Permissions by role:**
- Client: ManageBookings
- Trainer: ManageBookings, ManageSchedule
- Admin: All permissions
- SuperAdmin: All permissions

### Venue / Room / Station

```
Venue                    Room                      Station
├── id                   ├── id                    ├── id
├── name                 ├── venueId → Venue       ├── roomId → Room
├── address              ├── name                  ├── name
├── timezone             ├── capacity: PositiveInt  ├── stationType
├── status               ├── roomType              └── status
├── createdAt            ├── stations: Int
└── updatedAt            └── createdAt

RoomType: Standard | ReformerStudio | YogaStudio | Gym
StationType: Reformer | Cadillac | Chair | Barrel | Generic
StationStatus: Available | Maintenance | OutOfService
```

### ServiceDefinition

Template for what can be booked:

```
ServiceDefinition
├── id
├── name: NonEmptyString
├── category: ServiceCategory
├── durationMinutes: PositiveInt
├── capacity: PositiveInt
├── requiresStation: Boolean     ← true for reformer Pilates
├── price: Money
└── createdAt

ServiceCategory: IndividualTraining | GroupClass | ReformerPilates | Yoga | Spinning | Custom
```

### ClassDefinition / ClassInstance

```
ClassDefinition (template)          ClassInstance (occurrence)
├── id                              ├── id
├── serviceDefinitionId             ├── classDefinitionId → ClassDefinition
├── venueId → Venue                 ├── startTime: Instant
├── roomId → Room                   ├── endTime: Instant
├── instructorId → User             ├── currentBookings: Int
├── name                            ├── capacity: Int
├── capacity: PositiveInt           ├── status: ClassInstanceStatus
├── durationMinutes: PositiveInt    └── createdAt
├── bookingWindow: BookingWindow
├── cancellationPolicyId?           ClassInstanceStatus:
└── createdAt                         Scheduled | InProgress | Completed | Cancelled

BookingWindow { minAdvanceMinutes, maxAdvanceDays }
```

### WeeklySchedule

Defines recurring patterns that generate ClassInstances:

```
WeeklySchedule
├── id
├── classDefinitionId → ClassDefinition
├── dayOfWeek: DayOfWeek (MONDAY..SUNDAY)
├── startTime: LocalTime
├── effectiveFrom: LocalDate
└── effectiveUntil: Option[LocalDate]
```

The `RecurrenceEngine` generates ClassInstances from WeeklySchedules for a given date range, respecting timezone conversions.

## Booking Aggregate

### Booking

```
Booking
├── id
├── userId → User
├── classInstanceId → ClassInstance
├── stationId: Option[StationId]    ← for reformer classes
├── status: BookingStatus
├── cancellationFee: Option[BigDecimal]
├── createdAt
└── updatedAt
```

### Booking State Machine

```
                    ┌─────────────┐
                    │   Pending    │
                    └──────┬──────┘
                           │
                    ┌──────┴──────┐
              ┌─────┤  Confirmed  ├─────┐
              │     └──────┬──────┘     │
              │            │            │
       ┌──────┴──────┐ ┌──┴───┐ ┌──────┴──────┐
       │  Cancelled   │ │CheckIn│ │   NoShow    │
       └─────────────┘ └──┬───┘ └─────────────┘
                           │
                    ┌──────┴──────┐
                    │  Completed   │
                    └─────────────┘
```

**Valid transitions:**
| From | To |
|------|----|
| Pending | Confirmed, Cancelled |
| Confirmed | CheckedIn, Cancelled, NoShow |
| CheckedIn | Completed |

**Terminal states:** Completed, Cancelled, NoShow

### Business Rules

1. **Capacity check**: `currentBookings < capacity` (class-level)
2. **Station availability**: For reformer classes, station must exist in the room
3. **Booking window**: `minAdvanceMinutes < timeUntilClass < maxAdvanceDays`
4. **Duplicate prevention**: One active booking per user per class instance
5. **Cancellation fees**: Calculated based on CancellationPolicy rules and notice period

### CancellationPolicy

```
CancellationPolicy
├── id
├── name
└── rules: List[CancellationRule] (sorted by noticePeriodMinutes DESC)

CancellationRule
├── noticePeriodMinutes: Int    ← threshold in minutes before class
├── feeType: CancellationFeeType
└── feeValue: Option[BigDecimal]

CancellationFeeType: NoFee | FixedFee | Percentage | FullPrice | CreditForfeit
```

**Fee calculation example** (rules sorted DESC by notice period):
- 1440+ min (24h+): NoFee
- 360+ min (6h+): FixedFee(200 CZK)
- 60+ min (1h+): Percentage(50%)
- 0+ min: FullPrice

### WaitlistEntry

```
WaitlistEntry
├── id
├── classInstanceId → ClassInstance
├── userId → User
├── position: Int
├── status: WaitlistStatus
├── offerExpiresAt: Option[Instant]  ← 30 min window when offered
└── createdAt

WaitlistStatus: Waiting → Offered → Converted | Expired | Removed
```

**Waitlist flow:**
1. Class is full → user joins waitlist (status: Waiting)
2. Booking cancelled → next waitlist entry promoted (status: Offered, 30 min expiry)
3. User accepts within window → WaitlistConverted event, booking created
4. Offer expires → status: Expired, promote next entry

## Billing Model

### SubscriptionPlan / Subscription

```
SubscriptionPlan                    Subscription
├── id                              ├── id
├── name                            ├── userId → User
├── price: Money                    ├── planId → SubscriptionPlan
├── interval: BillingInterval       ├── status: SubscriptionStatus
├── includedBookings: Option[Int]   ├── currentPeriodStart
├── includedCredits: Option[Int]    ├── currentPeriodEnd
├── allowedServiceTypes             ├── pausedAt: Option[Instant]
└── createdAt                       ├── resumesAt: Option[Instant]
                                    └── createdAt

BillingInterval: Monthly | Quarterly | Yearly
SubscriptionStatus: Active | Paused | PastDue | Cancelled | Expired
```

### PackageDefinition / CreditPackage

```
PackageDefinition                   CreditPackage
├── id                              ├── id
├── name                            ├── userId → User
├── credits: PositiveInt            ├── packageDefinitionId
├── price: Money                    ├── totalCredits
├── validityDays: PositiveInt       ├── remainingCredits
├── allowedServiceTypes             ├── expiresAt: Instant
└── createdAt                       ├── status: CreditPackageStatus
                                    └── createdAt

CreditPackageStatus: Active | Expired | Depleted
```

**Credit deduction**: Packages expiring soonest are used first (FIFO by `expiresAt`).

## Domain Events

```
trait DomainEvent {
  def eventId: DomainEventId
  def occurredAt: Instant
  def aggregateId: String
  def eventType: String
}
```

| Event | Trigger | Side Effects |
|-------|---------|-------------|
| BookingCreated | New booking | - |
| BookingConfirmed | Booking confirmed | Send confirmation notification |
| BookingCancelled | Booking cancelled | Promote waitlist, send cancellation notice |
| BookingCheckedIn | User checks in | - |
| BookingNoShow | Marked as no-show | - |
| WaitlistOffered | Spot opens up | Send offer notification with 30 min deadline |
| WaitlistConverted | Offer accepted | Create booking |
