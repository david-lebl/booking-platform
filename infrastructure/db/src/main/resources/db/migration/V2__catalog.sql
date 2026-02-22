CREATE TABLE venues (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    address     TEXT NOT NULL,
    timezone    VARCHAR(100) NOT NULL DEFAULT 'UTC',
    status      VARCHAR(50) NOT NULL DEFAULT 'Active',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE rooms (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venue_id    UUID NOT NULL REFERENCES venues(id),
    name        VARCHAR(255) NOT NULL,
    capacity    INT NOT NULL CHECK (capacity > 0),
    room_type   VARCHAR(50) NOT NULL DEFAULT 'Standard',
    stations    INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rooms_venue_id ON rooms(venue_id);

CREATE TABLE stations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id         UUID NOT NULL REFERENCES rooms(id),
    name            VARCHAR(255) NOT NULL,
    station_type    VARCHAR(50) NOT NULL DEFAULT 'Generic',
    status          VARCHAR(50) NOT NULL DEFAULT 'Available'
);

CREATE INDEX idx_stations_room_id ON stations(room_id);

CREATE TABLE service_definitions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    category            VARCHAR(50) NOT NULL,
    duration_minutes    INT NOT NULL CHECK (duration_minutes > 0),
    capacity            INT NOT NULL CHECK (capacity > 0),
    requires_station    BOOLEAN NOT NULL DEFAULT FALSE,
    price_amount        DECIMAL(10,2) NOT NULL DEFAULT 0,
    price_currency      VARCHAR(3) NOT NULL DEFAULT 'CZK',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_service_definitions_category ON service_definitions(category);
