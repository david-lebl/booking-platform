# Architecture

## Design Principles

The platform follows **Domain-Driven Design (DDD)** with **Hexagonal Architecture** (Ports & Adapters):

1. **Domain at the center** - Business logic has zero infrastructure dependencies
2. **Ports define boundaries** - Trait interfaces in `core/` describe what the domain needs
3. **Adapters implement ports** - Infrastructure modules provide concrete implementations
4. **Dependency inversion** - All arrows point inward toward the domain

## Module Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         app (Main)                              │
│  ZIOAppDefault  ·  AppConfig  ·  AppLayers (ZLayer composition) │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │  rest-api-ctrl    │  │  http-client      │  │ payment-gw    │ │
│  │  Controllers      │  │  iCal export      │  │ NoOp stub     │ │
│  │  Auth middleware   │  │  Notification     │  │               │ │
│  │  Route composition │  │  sender (logging) │  │               │ │
│  └────────┬─────────┘  └────────┬─────────┘  └──────┬────────┘ │
│           │                     │                    │          │
│  ┌────────┴─────────┐          │                    │          │
│  │  tapir-api        │          │                    │          │
│  │  Endpoint defs    │          │                    │          │
│  │  Error mapping    │          │                    │          │
│  │  Tapir schemas    │          │                    │          │
│  └────────┬─────────┘          │                    │          │
│           │                     │                    │          │
│  ┌────────┴─────────┐  ┌──────┴────────────────────┴────────┐ │
│  │  infra/db         │  │                                     │ │
│  │  In-memory repos  │  │              core                   │ │
│  │  Flyway migrations│  │  ┌─────────┐ ┌──────────┐          │ │
│  │  (future: Typo)   │  │  │identity │ │ catalog  │          │ │
│  └───────────────────┘  │  ├─────────┤ ├──────────┤          │ │
│                         │  │scheduling│ │ booking  │          │ │
│                         │  ├─────────┤ ├──────────┤          │ │
│                         │  │ billing │ │notificatn│          │ │
│                         │  ├─────────┤ └──────────┘          │ │
│                         │  │ common  │ (ValueObjects, Errors │ │
│                         │  │         │  Events, EventBus)    │ │
│                         │  └─────────┘                        │ │
│                         └─────────────────────────────────────┘ │
│                                    │                            │
│  ┌─────────────────────────────────┴───────────────────────────┐│
│  │                   shared (JVM + JS)                         ││
│  │        Typed IDs  ·  API DTOs  ·  Shared enums              ││
│  └─────────────────────────────────────────────────────────────┘│
│                                    │                            │
│  ┌─────────────────────────────────┴───────────────────────────┐│
│  │                     ui (Scala.js)                           ││
│  │           Laminar  ·  Waypoint  ·  API client               ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

## Bounded Contexts

### 1. Identity & Access

Manages users, roles, and authentication.

- **Entities**: User
- **Enums**: UserRole (Client, Trainer, Admin, SuperAdmin), UserStatus, Permission
- **Ports**: UserRepository, AuthService
- **Service**: UserService

Authentication is a port - the current implementation (`StubAuthService`) treats the bearer token as a user UUID. Replace with JWT/OAuth in production.

### 2. Catalog

Manages the physical infrastructure and service offerings.

- **Entities**: Venue, Room, Station, ServiceDefinition
- **Enums**: VenueStatus, RoomType, StationType, StationStatus, ServiceCategory
- **Ports**: VenueRepository, RoomRepository, StationRepository, ServiceDefinitionRepository
- **Service**: CatalogService

Stations are specific physical equipment (reformer machines) within rooms, enabling station-level booking for Pilates classes.

### 3. Scheduling

Manages class templates, recurring schedules, and specific occurrences.

- **Entities**: ClassDefinition, ClassInstance, WeeklySchedule, InstructorAvailability
- **Value Objects**: BookingWindow (min/max advance time)
- **Ports**: ClassDefinitionRepository, ClassInstanceRepository, WeeklyScheduleRepository
- **Service**: SchedulingService
- **Engine**: RecurrenceEngine (generates ClassInstances from WeeklySchedules)

### 4. Booking (Core Aggregate)

The central context managing the booking lifecycle.

- **Entities**: Booking, WaitlistEntry, CancellationPolicy, CancellationRule
- **State Machine**: BookingStateMachine (Pending → Confirmed → CheckedIn → Completed)
- **Ports**: BookingRepository, WaitlistRepository, CancellationPolicyRepository
- **Services**: BookingService, WaitlistService

### 5. Billing

Manages subscriptions, credit packages, and payments.

- **Entities**: SubscriptionPlan, Subscription, PackageDefinition, CreditPackage
- **Ports**: SubscriptionPlanRepository, SubscriptionRepository, PackageDefinitionRepository, CreditPackageRepository, PaymentGateway
- **Service**: BillingService

### 6. Notification

Event-driven notifications and calendar exports.

- **Ports**: NotificationSender, CalendarExporter
- **Implementations**: LoggingNotificationSender (stub), ICalCalendarExporter (iCal4j)

## Cross-Cutting Concerns

### Domain Events

Events flow through an in-memory `EventBus` (ZIO Ref-based):

```
BookingConfirmed → send confirmation notification
BookingCancelled → promote waitlist, send cancellation notice
WaitlistOffered  → send time-limited offer notification
```

### Error Handling

`DomainError` enum in core → mapped to HTTP status codes in tapir-api:

| Domain Error | HTTP Status |
|-------------|-------------|
| NotFound | 404 |
| ValidationError | 400 |
| ConflictError | 409 |
| ClassFull | 409 |
| BookingWindowClosed | 400 |
| InvalidStateTransition | 400 |
| InsufficientCredits | 402 |
| AuthenticationError | 401 |
| AuthorizationError | 403 |

### ZLayer Composition

All dependencies are wired via ZIO's `ZLayer` in `AppLayers.scala`:

```
repositories (in-memory) + infrastructure (stubs) + EventBus
    >>> services (UserService, CatalogService, ...) + AuthService + AppConfig
```

## Multi-Tenancy Preparation

The schema is designed for future multi-tenancy:
- No `tenant_id` column yet
- Tables use UUIDs as primary keys
- Composite indexes can be extended with tenant_id
- Query scoping can be added via ZIO environment

## Technology Choices

| Decision | Rationale |
|----------|-----------|
| ZIO | Type-safe effects, built-in dependency injection via ZLayer |
| ZIO Prelude Newtypes | Compile-time type safety for value objects without runtime overhead |
| Tapir | Type-safe API definitions, auto-generated Swagger, framework-agnostic |
| Flyway | Industry-standard SQL migrations, version-controlled schema |
| In-memory repos first | Fast development cycle, defer DB complexity, easy testing |
| Opaque type IDs | Zero-cost type safety preventing ID mixups at compile time |
