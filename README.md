# Booking Platform

A fitness and reformer pilates booking platform built with Scala 3, ZIO, and Laminar.

## Technology Stack

### Backend
- **Scala 3.3.4** – Primary language
- **ZIO 2** – Effect system and runtime
- **ZIO Prelude** – Functional type-class library
- **Tapir** – Type-safe HTTP API definition
- **ZIO HTTP** – HTTP server
- **Quill + JDBC** – Type-safe SQL queries
- **PostgreSQL** – Database
- **Flyway** – Database migrations
- **ZIO Config** – Configuration management
- **ZIO Logging / SLF4J** – Structured logging

### Frontend
- **Scala.js** – Scala compiled to JavaScript
- **Laminar** – Reactive UI library with signal-based routing

## Architecture

The backend follows **Domain-Driven Design (DDD)** principles:

```
com.booking
├── domain/           ← Core business logic (no dependencies)
│   ├── model/        ← Entities, value objects, aggregates
│   ├── repository/   ← Repository interfaces (ports)
│   └── service/      ← Domain services
├── application/      ← Use case orchestration
├── infrastructure/   ← Repository implementations, DB, config
└── presentation/     ← HTTP API (Tapir endpoints + DTOs)
```

### Domain Model

| Aggregate    | Description                              |
|-------------|------------------------------------------|
| `Member`     | Registered user who books classes        |
| `Instructor` | Teacher who leads class sessions         |
| `ClassType`  | Type of class (e.g., Reformer Pilates)   |
| `Studio`     | Physical location with equipment         |
| `ClassSession` | Scheduled occurrence of a class        |
| `Booking`    | A member's reservation for a session     |

## Getting Started

### Prerequisites
- JDK 17+
- sbt 1.10.6+
- Docker & Docker Compose

### Run with Docker Compose

```bash
docker compose up -d postgres
```

### Run Backend (in-memory mode)

```bash
sbt "backend/run"
```

The API will be available at `http://localhost:8080/api`
Swagger UI: `http://localhost:8080/docs`

### Build Frontend

```bash
sbt "frontend/fastLinkJS"
```

### Run Tests

```bash
sbt "backend/test"
```

## API Endpoints

### Members
- `POST   /api/members`              – Register a member
- `GET    /api/members`              – List all members
- `GET    /api/members/:id`          – Get member by ID
- `GET    /api/members/:id/bookings` – Get member's bookings

### Class Types
- `POST   /api/class-types`          – Create a class type
- `GET    /api/class-types`          – List class types

### Sessions
- `POST   /api/sessions`             – Schedule a class session
- `GET    /api/sessions`             – List upcoming sessions
- `GET    /api/sessions/:id`         – Get session by ID

### Instructors & Studios
- `GET    /api/instructors`          – List instructors
- `GET    /api/studios`              – List studios

### Bookings
- `POST   /api/bookings`             – Create a booking
- `GET    /api/bookings/:id`         – Get booking by ID
- `DELETE /api/bookings/:id?memberId=…` – Cancel a booking

## Extension Points

The platform is designed for easy extension:
- **Wellness services** – Add new `ClassType` categories
- **Salons** – Introduce a new bounded context for appointment-based services
- **Payments** – Add a `Payment` aggregate with Stripe integration
- **Memberships** – Expand `MembershipType` with recurring billing
