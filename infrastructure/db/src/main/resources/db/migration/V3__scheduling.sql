CREATE TABLE cancellation_policies (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cancellation_rules (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id               UUID NOT NULL REFERENCES cancellation_policies(id) ON DELETE CASCADE,
    notice_period_minutes   INT NOT NULL,
    fee_type                VARCHAR(50) NOT NULL,
    fee_value               DECIMAL(10,2),
    sort_order              INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cancellation_rules_policy_id ON cancellation_rules(policy_id);

CREATE TABLE class_definitions (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_definition_id           UUID NOT NULL REFERENCES service_definitions(id),
    venue_id                        UUID NOT NULL REFERENCES venues(id),
    room_id                         UUID NOT NULL REFERENCES rooms(id),
    instructor_id                   UUID NOT NULL REFERENCES users(id),
    name                            VARCHAR(255) NOT NULL,
    capacity                        INT NOT NULL CHECK (capacity > 0),
    duration_minutes                INT NOT NULL CHECK (duration_minutes > 0),
    booking_window_min_advance_min  INT NOT NULL DEFAULT 0,
    booking_window_max_advance_days INT NOT NULL DEFAULT 30,
    cancellation_policy_id          UUID REFERENCES cancellation_policies(id),
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_class_definitions_venue_id ON class_definitions(venue_id);
CREATE INDEX idx_class_definitions_instructor_id ON class_definitions(instructor_id);

CREATE TABLE class_instances (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_definition_id UUID NOT NULL REFERENCES class_definitions(id),
    start_time          TIMESTAMPTZ NOT NULL,
    end_time            TIMESTAMPTZ NOT NULL,
    current_bookings    INT NOT NULL DEFAULT 0,
    capacity            INT NOT NULL CHECK (capacity > 0),
    status              VARCHAR(50) NOT NULL DEFAULT 'Scheduled',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_class_instances_class_def ON class_instances(class_definition_id);
CREATE INDEX idx_class_instances_start_time ON class_instances(start_time);
CREATE INDEX idx_class_instances_status ON class_instances(status);

CREATE TABLE weekly_schedules (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_definition_id UUID NOT NULL REFERENCES class_definitions(id),
    day_of_week         VARCHAR(10) NOT NULL,
    start_time          TIME NOT NULL,
    effective_from      DATE NOT NULL,
    effective_until     DATE
);

CREATE INDEX idx_weekly_schedules_class_def ON weekly_schedules(class_definition_id);

CREATE TABLE instructor_availability (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instructor_id   UUID NOT NULL REFERENCES users(id),
    day_of_week     VARCHAR(10) NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL
);

CREATE INDEX idx_instructor_availability_instructor ON instructor_availability(instructor_id);
