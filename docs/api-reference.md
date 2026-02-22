# API Reference

Base URL: `/api/v1`

Swagger UI: `/docs` (auto-generated from Tapir endpoint definitions)

## Authentication

Secured endpoints require a bearer token header:

```
Authorization: Bearer <user-uuid>
```

In development, the stub auth service treats the token value as a user UUID directly. In production, replace with JWT/OAuth.

## Error Response Format

All errors return:

```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable description"
}
```

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `NOT_FOUND` | 404 | Entity not found |
| `VALIDATION_ERROR` | 400 | Input validation failed |
| `CONFLICT` | 409 | Duplicate or conflicting state |
| `CLASS_FULL` | 409 | No available spots |
| `STATION_UNAVAILABLE` | 409 | Station not available |
| `BOOKING_WINDOW_CLOSED` | 400 | Outside booking window |
| `DUPLICATE_BOOKING` | 409 | User already booked this class |
| `INVALID_TRANSITION` | 400 | Invalid status change |
| `INSUFFICIENT_CREDITS` | 402 | Not enough credits |
| `PAYMENT_FAILED` | 402 | Payment processing error |
| `UNAUTHORIZED` | 401 | Invalid or missing auth |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `INTERNAL_ERROR` | 500 | Server error |

---

## Venues

### Create Venue

```
POST /api/v1/venues
```

**Request:**
```json
{
  "name": "Downtown Studio",
  "address": "123 Main St, Prague",
  "timezone": "Europe/Prague"
}
```

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "name": "Downtown Studio",
  "address": "123 Main St, Prague",
  "timezone": "Europe/Prague",
  "status": "Active",
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-01-01T00:00:00Z"
}
```

### Get Venue

```
GET /api/v1/venues/{venueId}
```

### List Venues

```
GET /api/v1/venues
```

**Response:** `200 OK` — Array of VenueResponse

### Update Venue

```
PUT /api/v1/venues/{venueId}
```

**Request:** (all fields optional)
```json
{
  "name": "Updated Name",
  "address": "456 New St",
  "timezone": "Europe/Berlin",
  "status": "Inactive"
}
```

---

## Rooms

### Create Room

```
POST /api/v1/rooms
```

**Request:**
```json
{
  "venueId": "uuid",
  "name": "Reformer Studio A",
  "capacity": 12,
  "roomType": "ReformerStudio",
  "stations": 12
}
```

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "venueId": "uuid",
  "name": "Reformer Studio A",
  "capacity": 12,
  "roomType": "ReformerStudio",
  "stations": 12,
  "createdAt": "2026-01-01T00:00:00Z"
}
```

### List Rooms by Venue

```
GET /api/v1/venues/{venueId}/rooms
```

---

## Stations

### Create Station

```
POST /api/v1/stations
```

**Request:**
```json
{
  "roomId": "uuid",
  "name": "Reformer #1",
  "stationType": "Reformer"
}
```

Station types: `Reformer`, `Cadillac`, `Chair`, `Barrel`, `Generic`

### List Stations by Room

```
GET /api/v1/rooms/{roomId}/stations
```

---

## Service Definitions

### Create Service Definition

```
POST /api/v1/service-definitions
```

**Request:**
```json
{
  "name": "Reformer Pilates Group",
  "category": "ReformerPilates",
  "durationMinutes": 55,
  "capacity": 12,
  "requiresStation": true,
  "priceAmount": 450.00,
  "priceCurrency": "CZK"
}
```

Categories: `IndividualTraining`, `GroupClass`, `ReformerPilates`, `Yoga`, `Spinning`, `Custom`

### List Service Definitions

```
GET /api/v1/service-definitions
```

---

## Users

### Create User

```
POST /api/v1/users
```

**Request:**
```json
{
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+420123456789",
  "role": "Client"
}
```

Roles: `Client`, `Trainer`, `Admin`, `SuperAdmin`

### Get User

```
GET /api/v1/users/{userId}
```

### List Users

```
GET /api/v1/users
```

### Update User

```
PUT /api/v1/users/{userId}
```

**Request:** (all fields optional)
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+420987654321",
  "status": "Suspended"
}
```

---

## Class Definitions

### Create Class Definition

```
POST /api/v1/class-definitions
```

**Request:**
```json
{
  "serviceDefinitionId": "uuid",
  "venueId": "uuid",
  "roomId": "uuid",
  "instructorId": "uuid",
  "name": "Morning Reformer",
  "capacity": 12,
  "durationMinutes": 55,
  "bookingWindowMinAdvanceMinutes": 60,
  "bookingWindowMaxAdvanceDays": 14,
  "cancellationPolicyId": "uuid or null"
}
```

### Get Class Definition

```
GET /api/v1/class-definitions/{classDefinitionId}
```

### List Class Definitions

```
GET /api/v1/class-definitions
```

---

## Weekly Schedules

### Create Weekly Schedule

```
POST /api/v1/weekly-schedules
```

**Request:**
```json
{
  "classDefinitionId": "uuid",
  "dayOfWeek": "MONDAY",
  "startTime": "09:00",
  "effectiveFrom": "2026-01-01",
  "effectiveUntil": "2026-06-30"
}
```

Day values: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`

---

## Class Instances

### Generate Instances

Creates class instances from weekly schedules for a date range.

```
POST /api/v1/class-instances/generate
```

**Request:**
```json
{
  "classDefinitionId": "uuid",
  "fromDate": "2026-02-01",
  "toDate": "2026-02-28"
}
```

**Response:** `200 OK` — Array of generated ClassInstanceResponse

### List Instances

```
GET /api/v1/class-instances?from=2026-02-01T00:00:00Z&to=2026-02-28T23:59:59Z
```

Query parameters use ISO-8601 instant format.

---

## Bookings

### Create Booking (authenticated)

```
POST /api/v1/bookings
Authorization: Bearer <user-uuid>
```

**Request:**
```json
{
  "classInstanceId": "uuid",
  "stationId": "uuid or null"
}
```

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "userId": "uuid",
  "classInstanceId": "uuid",
  "stationId": "uuid or null",
  "status": "Confirmed",
  "cancellationFee": null,
  "createdAt": "2026-02-01T10:00:00Z",
  "updatedAt": "2026-02-01T10:00:00Z"
}
```

### Get Booking

```
GET /api/v1/bookings/{bookingId}
```

### Get My Bookings (authenticated)

```
GET /api/v1/bookings/my
Authorization: Bearer <user-uuid>
```

### Cancel Booking (authenticated)

```
POST /api/v1/bookings/{bookingId}/cancel
Authorization: Bearer <user-uuid>
```

### Check In

```
POST /api/v1/bookings/{bookingId}/check-in
```

### Mark No Show

```
POST /api/v1/bookings/{bookingId}/no-show
```

---

## Waitlist

### Join Waitlist (authenticated)

```
POST /api/v1/class-instances/{classInstanceId}/waitlist
Authorization: Bearer <user-uuid>
```

### Accept Waitlist Offer (authenticated)

```
POST /api/v1/waitlist/{waitlistEntryId}/accept
Authorization: Bearer <user-uuid>
```

Must be accepted within 30 minutes of the offer.

### Get Waitlist

```
GET /api/v1/class-instances/{classInstanceId}/waitlist
```

---

## Cancellation Policies

### Create Cancellation Policy

```
POST /api/v1/cancellation-policies
```

**Request:**
```json
{
  "name": "Standard Policy",
  "rules": [
    { "noticePeriodMinutes": 1440, "feeType": "NoFee", "feeValue": null },
    { "noticePeriodMinutes": 360, "feeType": "FixedFee", "feeValue": 200 },
    { "noticePeriodMinutes": 0, "feeType": "FullPrice", "feeValue": null }
  ]
}
```

Fee types: `NoFee`, `FixedFee`, `Percentage`, `FullPrice`, `CreditForfeit`

### List Cancellation Policies

```
GET /api/v1/cancellation-policies
```

---

## Billing

### Create Subscription Plan

```
POST /api/v1/subscription-plans
```

**Request:**
```json
{
  "name": "Premium Monthly",
  "priceAmount": 2500.00,
  "priceCurrency": "CZK",
  "interval": "Monthly",
  "includedBookings": 8,
  "includedCredits": null,
  "allowedServiceTypes": ["GroupClass", "ReformerPilates", "Yoga"]
}
```

### List Subscription Plans

```
GET /api/v1/subscription-plans
```

### Subscribe (authenticated)

```
POST /api/v1/subscriptions
Authorization: Bearer <user-uuid>
```

**Request:**
```json
{ "planId": "uuid" }
```

### Cancel Subscription (authenticated)

```
POST /api/v1/subscriptions/{subscriptionId}/cancel
Authorization: Bearer <user-uuid>
```

### Create Package Definition

```
POST /api/v1/package-definitions
```

**Request:**
```json
{
  "name": "10-Class Pack",
  "credits": 10,
  "priceAmount": 3500.00,
  "priceCurrency": "CZK",
  "validityDays": 90,
  "allowedServiceTypes": ["GroupClass", "ReformerPilates"]
}
```

### List Package Definitions

```
GET /api/v1/package-definitions
```

### Purchase Credit Package (authenticated)

```
POST /api/v1/credit-packages
Authorization: Bearer <user-uuid>
```

**Request:**
```json
{ "packageDefinitionId": "uuid" }
```

### List My Credit Packages (authenticated)

```
GET /api/v1/credit-packages/my
Authorization: Bearer <user-uuid>
```

---

## System

### Health Check

```
GET /api/v1/health
```

**Response:**
```json
{
  "status": "ok",
  "version": "0.1.0-SNAPSHOT",
  "timestamp": "2026-02-22T10:00:00Z"
}
```
