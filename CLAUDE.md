# CLAUDE.md - Project Instructions for Claude Code

## Project Overview

Fitness booking platform built with Scala 3 + ZIO + hexagonal architecture. Supports class bookings, reformer Pilates station reservations, credit packages, and subscriptions.

## Build & Run

```bash
sbt compile           # Compile all modules
sbt core/test         # Run unit tests (41 tests, fast, no Docker)
sbt app/run           # Start server (port 8080, in-memory repos)
sbt ui/fastLinkJS     # Build frontend JS
```

Integration tests require Docker:
```bash
sbt integrationTest/test
```

## Architecture

**DDD + Hexagonal (Ports & Adapters)**. Domain logic lives in `core/` with zero infrastructure dependencies. Infrastructure adapters implement port interfaces.

### Module Dependency Graph

```
shared(JS) <- ui
shared(JVM) <- core <- tapir-api <- infra/rest-api-controller
                     <- infra/db
                     <- infra/http-client
                     <- infra/payment-gateway-client
                     <- app (depends on all)
                     <- integration-test (depends on app)
```

### Bounded Contexts (in core/)

- `identity/` - Users, roles, permissions, auth port
- `catalog/` - Venues, rooms, stations, service definitions
- `scheduling/` - Class definitions, instances, weekly schedules, recurrence
- `booking/` - Bookings (state machine), waitlist, cancellation policies
- `billing/` - Subscriptions, credit packages, payment gateway port
- `notification/` - Notification sender + calendar export ports
- `common/` - Value objects, DomainError, DomainEvent, EventBus

## Code Conventions

### Value Objects

Use ZIO Prelude Newtypes. `Newtype.make()` returns `Validation`, not `Either`. Convert with `.toEither` or use `ValidationHelper.validate()` in controllers.

```scala
// Creating value objects
val email = Email.make("user@example.com")          // Validation[String, Email]
val result = email.toEither                          // Either[NonEmptyChunk[String], Email]

// In controllers, use the helper:
import ValidationHelper.validate
name <- validate(NonEmptyString.make(req.name))      // IO[DomainError, NonEmptyString]
```

### Typed IDs

All entity IDs are opaque types wrapping UUID in `shared/ids/`:

```scala
opaque type UserId = UUID
object UserId:
  def apply(value: UUID): UserId = value
  def generate: UserId = UUID.randomUUID()
  extension (id: UserId) def value: UUID = id
```

### Domain Errors

All domain operations return `IO[DomainError, A]`. Errors are mapped to HTTP status codes in `tapir-api/errors/ErrorMapping.scala`.

### Repository Pattern

Core defines trait ports, infrastructure provides implementations:
- `core/catalog/CatalogPorts.scala` defines `VenueRepository` trait
- `infrastructure/db/repositories/InMemoryCatalogRepositories.scala` implements it
- Services depend on ports, not implementations

### Service Layer Pattern

```scala
final case class CatalogService(venueRepo: VenueRepository, ...) {
  def createVenue(...): IO[DomainError, Venue] = ...
}
object CatalogService {
  val layer: URLayer[VenueRepository & ..., CatalogService] =
    ZLayer.fromFunction(CatalogService.apply)
}
```

### Tapir Endpoints

Defined in `tapir-api/endpoints/`, implemented in `infra/rest-api-controller/controllers/`. Use `.zServerLogic` for public endpoints, `.zServerSecurityLogic` + `.serverLogic` for authenticated.

### ZIO HTTP

ZIO HTTP 3.0.1 uses `zio.http.Routes`, not `HttpApp`. The routes type from Tapir is `Routes[Nothing, Response]` due to `zServerLogic` producing `Nothing` env.

## Database

- Flyway migrations in `infrastructure/db/src/main/resources/db/migration/`
- V1: users, V2: catalog, V3: scheduling, V4: bookings, V5: billing, V6: domain_events
- Currently using in-memory repositories (no DB required for dev)
- iCal4j needs custom resolver: `https://repo.mnode.org/releases`

## Testing

- Unit tests in `core/src/test/` - pure domain logic, no IO
- Integration tests in `integration-test/` - testcontainers PostgreSQL
- Test framework: `zio.test.sbt.ZTestFramework`

## Key Dependency Notes

- SBT 1.10.7 (not 2.x - plugins not available)
- `libraryDependencySchemes` needed for zio-schema version conflicts
- iCal4j 4.0.8 requires custom Maven repo
- Tapir endpoint lists need `.widen[Any]` or type ascription for Swagger generation

## File Naming

- `Models.scala` - Domain entities and value objects
- `*Ports.scala` or `*Repository.scala` - Port interfaces
- `*Service.scala` - Domain services with ZLayer companion
- `*Controller.scala` - REST API controllers
- `*Endpoints.scala` - Tapir endpoint definitions
