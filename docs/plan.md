# Implementation Plan

## Vision

A fitness booking platform supporting individual/group trainer sessions, reformer Pilates (station-based), and future extensibility to wellness and salons. Single-tenant now with multi-tenancy preparation.

## Completed Phases

### Phase 0: Project Skeleton

- [x] SBT build with 11 modules and dependency wiring
- [x] `project/build.properties`, `plugins.sbt`, `Dependencies.scala`
- [x] `.scalafmt.conf`, `.gitignore` for Scala/SBT
- [x] `sbt compile` succeeds for all modules

### Phase 1: Core Domain Kernel

- [x] `shared`: 16 typed IDs with zio-json codecs
- [x] `shared`: Shared enums (UserRole, BookingStatus, etc.)
- [x] `shared`: API DTOs (request/response models)
- [x] `core/common`: Value objects (Email, Money, Phone, etc.)
- [x] `core/common`: DomainError enum with 17 error variants
- [x] `core/common`: DomainEvent trait + 7 event types
- [x] `core/common`: In-memory EventBus (ZIO Ref-based)
- [x] Unit tests: 11 value object validation tests

### Phase 2: Catalog Context

- [x] `core/catalog`: Venue, Room, Station, ServiceDefinition models
- [x] `core/catalog`: Repository ports (4 interfaces)
- [x] `core/catalog`: CatalogService with CRUD operations
- [x] Flyway V1 (users) + V2 (catalog tables)
- [x] `tapir-api`: Catalog endpoints (10 endpoints)
- [x] `rest-api-controller`: CatalogController
- [x] In-memory repository implementations

### Phase 3: Identity Context

- [x] `core/identity`: User, UserRole, Permission models
- [x] `core/identity`: UserRepository, AuthService ports
- [x] `core/identity`: UserService with CRUD + email uniqueness
- [x] StubAuthService (bearer token = UUID)
- [x] User CRUD endpoints + controller
- [x] In-memory UserRepository

### Phase 4: Scheduling Context

- [x] `core/scheduling`: ClassDefinition, ClassInstance, WeeklySchedule
- [x] `core/scheduling`: BookingWindow value object
- [x] `core/scheduling`: RecurrenceEngine (generates instances from schedules)
- [x] `core/scheduling`: SchedulingService
- [x] Flyway V3 (scheduling tables)
- [x] Tapir endpoints: class CRUD, schedule management, instance generation
- [x] Unit tests: 5 recurrence engine tests

### Phase 5: Booking Context

- [x] `core/booking`: Booking, CancellationPolicy, CancellationRule
- [x] `core/booking`: BookingStateMachine (Pending → Confirmed → CheckedIn → Completed)
- [x] `core/booking`: BookingService (create, cancel, check-in, no-show, complete)
- [x] Flyway V4 (bookings + waitlist tables)
- [x] All booking API endpoints + controller
- [x] Unit tests: 14 state machine tests + 5 cancellation fee tests

### Phase 6: Waitlist

- [x] `core/booking`: WaitlistEntry model + WaitlistService
- [x] Join waitlist, promote next, accept offer, remove
- [x] 30-minute offer expiration window
- [x] Domain event integration (WaitlistOffered, WaitlistConverted)
- [x] API endpoints: join, accept, list waitlist

### Phase 7: Billing Context

- [x] `core/billing`: SubscriptionPlan, Subscription, PackageDefinition, CreditPackage
- [x] `core/billing`: BillingService (subscribe, cancel, pause, purchase credits, deduct)
- [x] Flyway V5 (billing tables + join tables)
- [x] NoOp PaymentGateway stub
- [x] API endpoints for plans, subscriptions, packages, credits

### Phase 8: Notification Context

- [x] `core/notification`: NotificationSender, CalendarExporter ports
- [x] LoggingNotificationSender (stub, logs to console)
- [x] ICalCalendarExporter (iCal4j implementation)

### Phase 9: App Wiring

- [x] Main.scala with ZIOAppDefault
- [x] AppConfig (server host/port, database)
- [x] AppLayers: full ZLayer composition (15 repos + 3 infra + 6 services)
- [x] Health endpoint
- [x] Swagger UI at /docs
- [x] Logging via zio-logging + logback

### Phase 10: Frontend Basics (Partial)

- [x] Scala.js + Laminar build pipeline
- [x] Router with 4 pages (Home, Schedule, My Bookings, Login)
- [ ] API client service
- [ ] Auth flow with token storage
- [ ] Schedule page: weekly calendar view
- [ ] Booking flow: class detail → book → confirmation

## Remaining Phases

### Phase 10 (Continued): Frontend

- [ ] API client service consuming shared DTOs
- [ ] Auth state management (Var-based)
- [ ] Schedule page with weekly calendar view
- [ ] Booking flow: class detail → book → confirmation
- [ ] My Bookings page with cancel action
- [ ] Login page with token persistence

### Phase 11: Frontend Advanced

- [ ] Admin pages: manage venues, rooms, classes, schedules
- [ ] Station picker for reformer classes
- [ ] Waitlist UI (join, view position, accept offer)
- [ ] Billing pages (subscription plans, credit packages)
- [ ] Responsive design with Tailwind CSS

### Phase 12: PostgreSQL Integration

- [ ] Typo code generation from Flyway schema
- [ ] PostgreSQL repository implementations (replacing in-memory)
- [ ] Connection pooling (HikariCP / ZIO JDBC)
- [ ] Flyway migration runner in app startup
- [ ] Integration tests with testcontainers

### Phase 13: Production Hardening

- [ ] JWT / OAuth authentication (replace stub)
- [ ] CORS middleware configuration
- [ ] Rate limiting
- [ ] Request logging middleware
- [ ] Prometheus metrics endpoint
- [ ] Structured error logging
- [ ] Graceful shutdown

### Phase 14: Advanced Features

- [ ] Instructor availability management
- [ ] Buffer time between sessions validation
- [ ] Booking capacity tracking (increment/decrement on create/cancel)
- [ ] Credit deduction integration with BookingService
- [ ] Subscription usage tracking
- [ ] Expired offer / package cleanup (scheduled fiber)
- [ ] Domain event persistence to `domain_events` table

### Phase 15: Multi-Tenancy

- [ ] Add `tenant_id` to all tables (migration)
- [ ] TenantContext in ZIO environment
- [ ] Scoped repository queries
- [ ] Tenant resolution from JWT / subdomain
- [ ] Tenant-specific configuration

## Verification Checklist

- [x] `sbt compile` — all 11 modules compile
- [x] `sbt core/test` — 41 unit tests pass
- [ ] `sbt integrationTest/test` — integration tests with Docker
- [ ] `sbt app/run` + Swagger UI smoke test
- [ ] Frontend dev server renders all pages
- [ ] E2E flow: venue → class → schedule → instance → book → cancel → waitlist
