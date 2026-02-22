# Booking Platform

A full-featured booking platform for fitness sessions, built with **Scala 3**, **ZIO**, **Tapir**, and **Laminar**.

## Features

- **Fitness Session Booking** — Individual training, group training, and Reformer Pilates (station-based)
- **Subscription Models** — One-time, package (N sessions), and monthly unlimited plans
- **Booking Management** — Create, cancel, and track bookings with capacity enforcement
- **Late Cancellation Fees** — Automatic fee calculation based on cancellation timing:
  - More than 24 hours before: no fee
  - 2–24 hours before: 50% of session price
  - Less than 2 hours before: full session price
- **Waitlist** — Automatic waitlisting when sessions are full, with promotion to confirmed when spots open
- **iCal Integration** — Export bookings as `.ics` calendar feed
- **Swagger UI** — Auto-generated API documentation at `/docs`

## Architecture

The project follows **Domain-Driven Design (DDD)** and **Hexagonal Architecture** principles with a clean separation between domain logic, API definitions, infrastructure, and presentation.

### Module Structure

```
booking-platform/
├── core/                  # Domain model, ports (interfaces), and domain services
│   └── domain/            # Entities, enums, value objects, error types
│   └── port/              # Repository and gateway traits (ports)
│   └── service/           # Domain service traits and implementations
├── tapir-api/             # API layer — Tapir endpoint definitions and DTOs
│   └── dto/               # Request/response models with JSON codecs
│   └── endpoint/          # Tapir endpoint definitions
├── infrastructure/        # Adapters — in-memory repos, mock gateways, controllers
│   └── repository/        # In-memory repository implementations (ZIO Ref)
│   └── gateway/           # Mock payment, notification, and iCal services
│   └── controller/        # REST API controllers wiring endpoints to services
├── app/                   # Application entry point and dependency wiring
├── integration-test/      # Integration tests
└── ui/                    # Laminar (Scala.js) frontend
```

### Dependency Flow

```
core ← tapir-api ← infrastructure ← app
                                      ↑
                              integration-test

ui (independent Scala.js module)
```

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Scala 3.3.4 |
| Effect System | ZIO 2.1.6 |
| Validation | ZIO Prelude |
| API Definitions | Tapir 1.11.1 |
| HTTP Server | ZIO HTTP (via Tapir) |
| JSON | zio-json |
| Database | In-memory (ZIO Ref) |
| Frontend | Laminar 17.0.0 (Scala.js) |
| API Docs | Swagger UI |

## Getting Started

### Prerequisites

- JDK 17+
- sbt 1.10.7+
- Node.js (for Scala.js frontend)

### Run the Backend

```bash
sbt "app/run"
```

The API server starts on **http://localhost:8080** with Swagger UI at **http://localhost:8080/docs**.

### Build the Frontend

```bash
sbt "ui/fastLinkJS"
```

Then open `ui/index.html` in a browser (or serve it with any static file server).

### Run Tests

```bash
# Run integration tests
sbt "integration-test/test"

# Compile all modules
sbt compile
```

## API Endpoints

### Users
- `POST /api/users` — Create user
- `GET /api/users` — List all users
- `GET /api/users/:id` — Get user by ID
- `PUT /api/users/:id` — Update user

### Trainers
- `POST /api/trainers` — Create trainer
- `GET /api/trainers` — List all trainers
- `GET /api/trainers/:id` — Get trainer by ID

### Sessions
- `POST /api/sessions` — Create session template
- `GET /api/sessions` — List all sessions
- `GET /api/sessions/:id` — Get session by ID
- `POST /api/sessions/schedule` — Schedule a session instance
- `GET /api/sessions/scheduled` — List upcoming scheduled sessions
- `GET /api/sessions/scheduled/:id` — Get scheduled session by ID

### Bookings
- `POST /api/bookings` — Create booking (with optional subscription)
- `GET /api/bookings/:id` — Get booking by ID
- `POST /api/bookings/:id/cancel` — Cancel booking (with late fee calculation)
- `GET /api/users/:userId/bookings` — Get user's bookings

### Subscriptions
- `POST /api/subscription-plans` — Create subscription plan
- `GET /api/subscription-plans` — List all plans
- `POST /api/subscriptions` — Subscribe to a plan
- `GET /api/subscriptions/:id` — Get subscription
- `POST /api/subscriptions/:id/cancel` — Cancel subscription
- `GET /api/users/:userId/subscriptions` — Get user's subscriptions

### Waitlist
- `POST /api/waitlist` — Join waitlist
- `DELETE /api/waitlist/:userId/:scheduledSessionId` — Leave waitlist
- `GET /api/waitlist/:scheduledSessionId` — Get waitlist entries

### Calendar
- `GET /api/users/:userId/calendar` — Export user's bookings as iCal feed

## Domain Model

### Session Types
- **IndividualTraining** — 1-on-1 personal training
- **GroupTraining** — Group fitness classes
- **ReformerPilates** — Station-based Pilates (with station count)

### Subscription Plan Types
- **OneTime** — Single session purchase
- **Package** — N sessions within a validity period
- **Monthly** — Unlimited sessions per month

### Booking Statuses
- **Confirmed** — Active booking
- **WaitListed** — On waitlist (session at capacity)
- **Cancelled** — Cancelled by user
- **Completed** — Session attended
- **NoShow** — User didn't attend