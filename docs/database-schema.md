# Database Schema

## Overview

The database uses PostgreSQL with Flyway-managed migrations. All tables use UUID primary keys (via `gen_random_uuid()`) and timestamps with time zone (`TIMESTAMPTZ`).

Migrations are in `infrastructure/db/src/main/resources/db/migration/`.

## Migration History

| Version | File | Description |
|---------|------|-------------|
| V1 | `V1__users.sql` | Users table |
| V2 | `V2__catalog.sql` | Venues, rooms, stations, service definitions |
| V3 | `V3__scheduling.sql` | Class definitions, instances, schedules, cancellation policies |
| V4 | `V4__bookings.sql` | Bookings, waitlist entries |
| V5 | `V5__billing.sql` | Subscription plans, subscriptions, packages, credits |
| V6 | `V6__domain_events.sql` | Domain event audit trail |

## Tables

### V1 - Users

```sql
users
├── id              UUID PK DEFAULT gen_random_uuid()
├── email           VARCHAR(255) NOT NULL UNIQUE
├── first_name      VARCHAR(255) NOT NULL
├── last_name       VARCHAR(255) NOT NULL
├── phone           VARCHAR(50)
├── role            VARCHAR(50) NOT NULL DEFAULT 'Client'
├── status          VARCHAR(50) NOT NULL DEFAULT 'Active'
├── created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
└── updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()

Indexes: email, role, status
```

### V2 - Catalog

```sql
venues
├── id              UUID PK
├── name            VARCHAR(255) NOT NULL
├── address         TEXT NOT NULL
├── timezone        VARCHAR(100) NOT NULL DEFAULT 'UTC'
├── status          VARCHAR(50) NOT NULL DEFAULT 'Active'
├── created_at      TIMESTAMPTZ
└── updated_at      TIMESTAMPTZ

rooms
├── id              UUID PK
├── venue_id        UUID NOT NULL FK → venues(id)
├── name            VARCHAR(255) NOT NULL
├── capacity        INT NOT NULL CHECK (> 0)
├── room_type       VARCHAR(50) NOT NULL DEFAULT 'Standard'
├── stations        INT NOT NULL DEFAULT 0
└── created_at      TIMESTAMPTZ

stations
├── id              UUID PK
├── room_id         UUID NOT NULL FK → rooms(id)
├── name            VARCHAR(255) NOT NULL
├── station_type    VARCHAR(50) NOT NULL DEFAULT 'Generic'
└── status          VARCHAR(50) NOT NULL DEFAULT 'Available'

service_definitions
├── id              UUID PK
├── name            VARCHAR(255) NOT NULL
├── category        VARCHAR(50) NOT NULL
├── duration_minutes INT NOT NULL CHECK (> 0)
├── capacity        INT NOT NULL CHECK (> 0)
├── requires_station BOOLEAN NOT NULL DEFAULT FALSE
├── price_amount    DECIMAL(10,2) NOT NULL DEFAULT 0
├── price_currency  VARCHAR(3) NOT NULL DEFAULT 'CZK'
└── created_at      TIMESTAMPTZ
```

### V3 - Scheduling

```sql
cancellation_policies
├── id              UUID PK
├── name            VARCHAR(255) NOT NULL
└── created_at      TIMESTAMPTZ

cancellation_rules
├── id              UUID PK
├── policy_id       UUID NOT NULL FK → cancellation_policies(id) ON DELETE CASCADE
├── notice_period_minutes INT NOT NULL
├── fee_type        VARCHAR(50) NOT NULL
├── fee_value       DECIMAL(10,2)
└── sort_order      INT NOT NULL DEFAULT 0

class_definitions
├── id              UUID PK
├── service_definition_id UUID NOT NULL FK → service_definitions(id)
├── venue_id        UUID NOT NULL FK → venues(id)
├── room_id         UUID NOT NULL FK → rooms(id)
├── instructor_id   UUID NOT NULL FK → users(id)
├── name            VARCHAR(255) NOT NULL
├── capacity        INT NOT NULL CHECK (> 0)
├── duration_minutes INT NOT NULL CHECK (> 0)
├── booking_window_min_advance_min INT NOT NULL DEFAULT 0
├── booking_window_max_advance_days INT NOT NULL DEFAULT 30
├── cancellation_policy_id UUID FK → cancellation_policies(id)
└── created_at      TIMESTAMPTZ

class_instances
├── id              UUID PK
├── class_definition_id UUID NOT NULL FK → class_definitions(id)
├── start_time      TIMESTAMPTZ NOT NULL
├── end_time        TIMESTAMPTZ NOT NULL
├── current_bookings INT NOT NULL DEFAULT 0
├── capacity        INT NOT NULL CHECK (> 0)
├── status          VARCHAR(50) NOT NULL DEFAULT 'Scheduled'
└── created_at      TIMESTAMPTZ

weekly_schedules
├── id              UUID PK
├── class_definition_id UUID NOT NULL FK → class_definitions(id)
├── day_of_week     VARCHAR(10) NOT NULL
├── start_time      TIME NOT NULL
├── effective_from  DATE NOT NULL
└── effective_until DATE

instructor_availability
├── id              UUID PK
├── instructor_id   UUID NOT NULL FK → users(id)
├── day_of_week     VARCHAR(10) NOT NULL
├── start_time      TIME NOT NULL
└── end_time        TIME NOT NULL
```

### V4 - Bookings

```sql
bookings
├── id              UUID PK
├── user_id         UUID NOT NULL FK → users(id)
├── class_instance_id UUID NOT NULL FK → class_instances(id)
├── station_id      UUID FK → stations(id)
├── status          VARCHAR(50) NOT NULL DEFAULT 'Pending'
├── cancellation_fee DECIMAL(10,2)
├── created_at      TIMESTAMPTZ
└── updated_at      TIMESTAMPTZ

Unique partial index: (user_id, class_instance_id) WHERE status NOT IN ('Cancelled', 'NoShow')

waitlist_entries
├── id              UUID PK
├── class_instance_id UUID NOT NULL FK → class_instances(id)
├── user_id         UUID NOT NULL FK → users(id)
├── position        INT NOT NULL
├── status          VARCHAR(50) NOT NULL DEFAULT 'Waiting'
├── offer_expires_at TIMESTAMPTZ
└── created_at      TIMESTAMPTZ
```

### V5 - Billing

```sql
subscription_plans
├── id              UUID PK
├── name            VARCHAR(255) NOT NULL
├── price_amount    DECIMAL(10,2) NOT NULL
├── price_currency  VARCHAR(3) NOT NULL DEFAULT 'CZK'
├── interval        VARCHAR(20) NOT NULL DEFAULT 'Monthly'
├── included_bookings INT
├── included_credits INT
└── created_at      TIMESTAMPTZ

subscription_plan_service_types    (join table)
├── plan_id         UUID FK → subscription_plans(id) ON DELETE CASCADE
└── service_type    VARCHAR(50) NOT NULL
    PK (plan_id, service_type)

subscriptions
├── id              UUID PK
├── user_id         UUID NOT NULL FK → users(id)
├── plan_id         UUID NOT NULL FK → subscription_plans(id)
├── status          VARCHAR(50) NOT NULL DEFAULT 'Active'
├── current_period_start TIMESTAMPTZ NOT NULL
├── current_period_end TIMESTAMPTZ NOT NULL
├── paused_at       TIMESTAMPTZ
├── resumes_at      TIMESTAMPTZ
└── created_at      TIMESTAMPTZ

package_definitions
├── id              UUID PK
├── name            VARCHAR(255) NOT NULL
├── credits         INT NOT NULL CHECK (> 0)
├── price_amount    DECIMAL(10,2) NOT NULL
├── price_currency  VARCHAR(3) NOT NULL DEFAULT 'CZK'
├── validity_days   INT NOT NULL CHECK (> 0)
└── created_at      TIMESTAMPTZ

package_definition_service_types    (join table)
├── package_id      UUID FK → package_definitions(id) ON DELETE CASCADE
└── service_type    VARCHAR(50) NOT NULL
    PK (package_id, service_type)

credit_packages
├── id              UUID PK
├── user_id         UUID NOT NULL FK → users(id)
├── package_definition_id UUID NOT NULL FK → package_definitions(id)
├── total_credits   INT NOT NULL
├── remaining_credits INT NOT NULL
├── expires_at      TIMESTAMPTZ NOT NULL
├── status          VARCHAR(50) NOT NULL DEFAULT 'Active'
└── created_at      TIMESTAMPTZ
```

### V6 - Domain Events

```sql
domain_events
├── id              UUID PK
├── event_type      VARCHAR(100) NOT NULL
├── aggregate_id    VARCHAR(255) NOT NULL
├── payload         JSONB NOT NULL
├── occurred_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
└── processed       BOOLEAN NOT NULL DEFAULT FALSE

Indexes: aggregate_id, event_type, occurred_at, unprocessed (partial)
```

## Multi-Tenancy Preparation

The schema is designed for future multi-tenancy addition:

1. Add `tenant_id UUID NOT NULL` column to all tables
2. Add `tenant_id` to all composite indexes
3. Add row-level security policies
4. Scope all queries via ZIO environment providing current tenant

No structural changes are needed — just column additions and index extensions.

## Index Strategy

| Table | Index | Purpose |
|-------|-------|---------|
| users | email | Unique lookup by email |
| users | role, status | Filter by role/status |
| rooms | venue_id | List rooms by venue |
| stations | room_id | List stations by room |
| class_instances | start_time | Date range queries |
| class_instances | class_definition_id | List instances per class |
| bookings | user_id | User booking history |
| bookings | class_instance_id | Class roster |
| bookings | (user_id, class_instance_id) partial | Prevent duplicate active bookings |
| waitlist_entries | class_instance_id | Waitlist per class |
| credit_packages | user_id, status | Active packages per user |
| domain_events | processed partial | Unprocessed event polling |
