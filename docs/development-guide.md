# Development Guide

## Prerequisites

- JDK 17+ (recommend Azul Zulu or Eclipse Temurin)
- SBT 1.10+
- Docker (for integration tests and PostgreSQL)
- Node.js 18+ (for frontend development)

## Getting Started

### 1. Clone and Build

```bash
git clone <repo-url>
cd booking-platform
sbt compile
```

### 2. Run the Server

```bash
sbt app/run
```

Starts on `http://localhost:8080` with in-memory repositories (no database needed).

### 3. Explore the API

Open Swagger UI at `http://localhost:8080/docs` to browse and test endpoints interactively.

### 4. Create Test Data

```bash
# Create a venue
curl -X POST http://localhost:8080/api/v1/venues \
  -H "Content-Type: application/json" \
  -d '{"name":"Studio Prague","address":"Vinohradska 10","timezone":"Europe/Prague"}'

# Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","firstName":"John","lastName":"Doe","role":"Client"}'
```

## Project Structure

```
booking-platform/
├── shared/                     Cross-compiled IDs + DTOs
├── core/                       Domain logic (THE source of truth)
│   └── src/main/scala/.../core/
│       ├── common/             Value objects, errors, events
│       ├── identity/           User, roles, auth port
│       ├── catalog/            Venues, rooms, stations, services
│       ├── scheduling/         Classes, schedules, recurrence
│       ├── booking/            Bookings, waitlist, cancellation
│       ├── billing/            Subscriptions, credits, payment port
│       └── notification/       Notification + calendar ports
├── tapir-api/                  Endpoint definitions
├── infrastructure/
│   ├── db/                     Repository implementations + migrations
│   ├── rest-api-controller/    HTTP controllers + routing
│   ├── http-client/            External service clients
│   └── payment-gateway-client/ Payment adapter
├── app/                        Main + config + wiring
├── integration-test/           E2E tests
└── ui/                         Laminar frontend
```

## Key Patterns

### Adding a New Entity

1. **Define the model** in `core/<context>/Models.scala`
2. **Define the port** (repository interface) in `core/<context>/<Context>Ports.scala`
3. **Add service methods** in `core/<context>/<Context>Service.scala`
4. **Add API DTOs** in `shared/models/ApiModels.scala`
5. **Add endpoint definitions** in `tapir-api/endpoints/`
6. **Implement the repository** in `infrastructure/db/repositories/`
7. **Add controller logic** in `infrastructure/rest-api-controller/controllers/`
8. **Wire into AppLayers** in `app/AppLayers.scala`
9. **Write migration** in `infrastructure/db/src/main/resources/db/migration/`

### Value Objects

Use ZIO Prelude Newtypes for type-safe wrappers:

```scala
type Email = Email.Type
object Email extends Newtype[String]:
  override inline def assertion: Assertion[String] =
    Assertion.matches("regex-pattern")
```

Key: `Newtype.make()` returns `Validation[String, T]`, not `Either`. Use `.toEither` or the `ValidationHelper`:

```scala
// In controllers:
import ValidationHelper.validate
name <- validate(NonEmptyString.make(req.name))  // IO[DomainError, NonEmptyString]
```

### Repository Ports

Define in `core/` as traits returning `IO[DomainError, _]`:

```scala
trait VenueRepository:
  def create(venue: Venue): IO[DomainError, Venue]
  def findById(id: VenueId): IO[DomainError, Option[Venue]]
  def findAll: IO[DomainError, List[Venue]]
  def update(venue: Venue): IO[DomainError, Venue]
```

### Service Layer

Services take repository ports as constructor parameters and expose a `ZLayer`:

```scala
final case class CatalogService(venueRepo: VenueRepository, ...):
  def createVenue(...): IO[DomainError, Venue] = ...

object CatalogService:
  val layer: URLayer[VenueRepository & ..., CatalogService] =
    ZLayer.fromFunction(CatalogService.apply)
```

### Tapir Endpoints

Define endpoint shape in `tapir-api/`:

```scala
val createVenue: Endpoint[Unit, CreateVenueRequest, ApiError, VenueResponse, Any] =
  BaseEndpoint.baseEndpoint.post
    .in("venues")
    .in(jsonBody[CreateVenueRequest])
    .out(jsonBody[VenueResponse])
    .tag("Venues")
```

Implement server logic in controllers:

```scala
CatalogEndpoints.createVenue.zServerLogic { req =>
  (for
    name <- validate(NonEmptyString.make(req.name))
    venue <- svc.createVenue(name, ...)
  yield toResponse(venue)).mapError(ErrorMapping.toApiError)
}
```

### Domain Events

Publish events from services:

```scala
_ <- eventBus.publish(BookingConfirmed(
  eventId = DomainEventId.generate,
  occurredAt = Instant.now(),
  bookingId = booking.id,
  ...
))
```

Subscribe to events (typically in app wiring):

```scala
eventBus.subscribe {
  case e: BookingCancelled => waitlistService.promoteNext(e.classInstanceId)
  case _ => ZIO.unit
}
```

## Testing

### Unit Tests

Pure domain logic tests in `core/src/test/`:

```bash
sbt core/test
```

Tests cover:
- Value object validation (Email, Phone, PositiveInt, Money)
- Booking state machine transitions
- Recurrence engine instance generation
- Cancellation fee calculation

### Integration Tests

End-to-end tests with testcontainers PostgreSQL:

```bash
sbt integrationTest/test   # Requires Docker
```

### Writing Tests

Use ZIO Test:

```scala
object MySpec extends ZIOSpecDefault:
  def spec = suite("MySuite")(
    test("my test") {
      assertTrue(1 + 1 == 2)
    },
    test("my ZIO test") {
      for
        result <- myService.doSomething()
      yield assertTrue(result == expected)
    }
  )
```

## Code Style

### Formatting

```bash
sbt scalafmt        # Format all code
sbt scalafmtCheck    # Check formatting
```

Config in `.scalafmt.conf`:
- Max column width: 120
- Scala 3 dialect
- Aligned tokens for `<-`, `=>`, `%`, `%%`

### Conventions

- Use Scala 3 syntax: `enum`, `given`, `extension`, braceless `for`/`if`
- Prefer `final case class` for domain entities
- Services are case classes with `ZLayer.fromFunction` companions
- Files named by primary type: `Models.scala`, `CatalogService.scala`
- Imports: `zio.*`, avoid wildcard for project packages

## Configuration

App configuration in `app/src/main/resources/application.conf`:

```hocon
server {
  host = "0.0.0.0"
  host = ${?SERVER_HOST}
  port = 8080
  port = ${?SERVER_PORT}
}

database {
  url = "jdbc:postgresql://localhost:5432/booking"
  url = ${?DATABASE_URL}
  user = "postgres"
  user = ${?DATABASE_USER}
  password = "postgres"
  password = ${?DATABASE_PASSWORD}
}
```

Environment variables override defaults.

## Database Migrations

Migrations are SQL files in `infrastructure/db/src/main/resources/db/migration/`:

```
V1__users.sql
V2__catalog.sql
V3__scheduling.sql
V4__bookings.sql
V5__billing.sql
V6__domain_events.sql
```

### Adding a New Migration

1. Create `V<N>__description.sql` following the naming convention
2. Write idempotent SQL (use `IF NOT EXISTS` where possible)
3. Always include appropriate indexes
4. Test with: start PostgreSQL, run Flyway, verify schema

### Typo Code Generation (Future)

When switching from in-memory to PostgreSQL repositories:

1. Start a PostgreSQL instance
2. Apply Flyway migrations
3. Run Typo generator to produce type-safe Row/Repo code
4. Implement repository adapters mapping domain types to/from generated rows
5. Commit generated code

## Troubleshooting

### Common Build Issues

**zio-schema version conflict**:
Already handled via `libraryDependencySchemes` in `build.sbt`.

**iCal4j not found on Maven Central**:
Uses custom resolver `https://repo.mnode.org/releases` configured in `build.sbt`.

**SBT version**:
Must use SBT 1.x (1.10.7). SBT 2.x plugins are not yet available.

**Tapir endpoint type mismatch**:
`zServerLogic` produces `ZServerEndpoint[Nothing, Any]`. For Swagger generation, the endpoint list is untyped. No `.widen` needed if you let Scala infer the type.
