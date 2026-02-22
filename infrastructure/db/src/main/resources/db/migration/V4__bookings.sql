CREATE TABLE bookings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id),
    class_instance_id   UUID NOT NULL REFERENCES class_instances(id),
    station_id          UUID REFERENCES stations(id),
    status              VARCHAR(50) NOT NULL DEFAULT 'Pending',
    cancellation_fee    DECIMAL(10,2),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_class_instance ON bookings(class_instance_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE UNIQUE INDEX idx_bookings_user_instance_active
    ON bookings(user_id, class_instance_id)
    WHERE status NOT IN ('Cancelled', 'NoShow');

CREATE TABLE waitlist_entries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_instance_id   UUID NOT NULL REFERENCES class_instances(id),
    user_id             UUID NOT NULL REFERENCES users(id),
    position            INT NOT NULL,
    status              VARCHAR(50) NOT NULL DEFAULT 'Waiting',
    offer_expires_at    TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_waitlist_class_instance ON waitlist_entries(class_instance_id);
CREATE INDEX idx_waitlist_user ON waitlist_entries(user_id);
CREATE INDEX idx_waitlist_status ON waitlist_entries(status);
