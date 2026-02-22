# Booking Platform

A fitness booking platform for individual/group trainer sessions and reformer Pilates, built with Scala 3, ZIO, and hexagonal architecture. Supports one-time bookings, credit packages, and monthly subscriptions.

## Quick Start

### Prerequisites

- JDK 17+
- SBT 1.10+
- PostgreSQL 15+ (or Docker)
- Node.js 18+ (for frontend dev)

### Run (in-memory, no DB required)

```bash
sbt app/run
```

The server starts on `http://localhost:8080` with Swagger UI at `http://localhost:8080/docs`.

### Run Tests

```bash
sbt core/test              # Unit tests (41 tests)
sbt integrationTest/test   # Integration tests (requires Docker)
```

### Build Frontend

```bash
sbt ui/fastLinkJS   # Development build
sbt ui/fullLinkJS    # Production build
```

## Architecture

The project follows **DDD + Hexagonal Architecture** with clean separation of domain logic from infrastructure.

```
shared (cross JVM/JS)   Typed IDs, API DTOs, shared enums
         |
       core             Domain models, ports (interfaces), services
         |
     tapir-api          Endpoint definitions, error mapping
         |
   infrastructure/      Adapters: DB repos, REST controllers, external clients
         |
        app             Main entry point, ZLayer wiring, config
```

See [docs/architecture.md](docs/architecture.md) for details.

## Project Structure

| Module | Description |
|--------|-------------|
| `shared` | Cross-compiled JVM/JS: typed IDs, API DTOs, enums |
| `core` | Domain model, ports, services (zero infrastructure deps) |
| `tapir-api` | Tapir endpoint definitions + error mapping |
| `infrastructure/db` | Repository implementations + Flyway migrations |
| `infrastructure/rest-api-controller` | HTTP controllers, auth middleware, routing |
| `infrastructure/http-client` | iCal export, notification sender stubs |
| `infrastructure/payment-gateway-client` | Payment gateway (NoOp stub) |
| `app` | ZIOAppDefault entry point, config, layer composition |
| `integration-test` | E2E tests with testcontainers |
| `ui` | Laminar + Waypoint frontend (Scala.js) |

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Scala 3 | 3.6.4 |
| Build | SBT | 1.10.7 |
| Effect System | ZIO + ZIO Prelude | 2.1.16 / 1.0.0-RC39 |
| API | Tapir + ZIO HTTP | 1.11.24 / 3.0.1 |
| Database | PostgreSQL + Flyway | 15+ / 10.22 |
| JSON | zio-json | 0.7.39 |
| Frontend | Laminar + Scala.js | 17.2.0 / 1.17.0 |
| Testing | ZIO Test + Testcontainers | 2.1.16 / 0.41.8 |

## API Endpoints

All endpoints are under `/api/v1/`. Swagger UI available at `/docs`.

| Context | Endpoints |
|---------|-----------|
| Venues | `POST/GET/PUT /venues`, `GET /venues/:id` |
| Rooms | `POST /rooms`, `GET /venues/:id/rooms` |
| Stations | `POST /stations`, `GET /rooms/:id/stations` |
| Services | `POST/GET /service-definitions` |
| Users | `POST/GET /users`, `GET/PUT /users/:id` |
| Classes | `POST/GET /class-definitions`, `GET /class-definitions/:id` |
| Schedules | `POST /weekly-schedules` |
| Instances | `POST /class-instances/generate`, `GET /class-instances` |
| Bookings | `POST /bookings`, `GET /bookings/:id`, `POST /:id/cancel`, `POST /:id/check-in` |
| Waitlist | `POST /class-instances/:id/waitlist`, `POST /waitlist/:id/accept` |
| Billing | `POST/GET /subscription-plans`, `POST /subscriptions`, `POST/GET /credit-packages` |
| System | `GET /health` |

Secured endpoints require `Authorization: Bearer <user-uuid>` header (stub auth for dev).

## Documentation

- [Architecture](docs/architecture.md) - System design, module dependencies, bounded contexts
- [Domain Model](docs/domain-model.md) - Entities, value objects, aggregates, business rules
- [API Reference](docs/api-reference.md) - Endpoint details, request/response formats
- [Database Schema](docs/database-schema.md) - Table definitions, migrations, indexes
- [Development Guide](docs/development-guide.md) - Setup, conventions, testing, adding features
- [Deployment](docs/deployment.md) - Configuration, Docker, production setup

## License

See [LICENSE](LICENSE) for details.
