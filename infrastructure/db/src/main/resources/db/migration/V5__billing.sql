CREATE TABLE subscription_plans (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    price_amount        DECIMAL(10,2) NOT NULL,
    price_currency      VARCHAR(3) NOT NULL DEFAULT 'CZK',
    interval            VARCHAR(20) NOT NULL DEFAULT 'Monthly',
    included_bookings   INT,
    included_credits    INT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE subscription_plan_service_types (
    plan_id         UUID NOT NULL REFERENCES subscription_plans(id) ON DELETE CASCADE,
    service_type    VARCHAR(50) NOT NULL,
    PRIMARY KEY (plan_id, service_type)
);

CREATE TABLE subscriptions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id),
    plan_id                 UUID NOT NULL REFERENCES subscription_plans(id),
    status                  VARCHAR(50) NOT NULL DEFAULT 'Active',
    current_period_start    TIMESTAMPTZ NOT NULL,
    current_period_end      TIMESTAMPTZ NOT NULL,
    paused_at               TIMESTAMPTZ,
    resumes_at              TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_user ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);

CREATE TABLE package_definitions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    credits             INT NOT NULL CHECK (credits > 0),
    price_amount        DECIMAL(10,2) NOT NULL,
    price_currency      VARCHAR(3) NOT NULL DEFAULT 'CZK',
    validity_days       INT NOT NULL CHECK (validity_days > 0),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE package_definition_service_types (
    package_id      UUID NOT NULL REFERENCES package_definitions(id) ON DELETE CASCADE,
    service_type    VARCHAR(50) NOT NULL,
    PRIMARY KEY (package_id, service_type)
);

CREATE TABLE credit_packages (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL REFERENCES users(id),
    package_definition_id   UUID NOT NULL REFERENCES package_definitions(id),
    total_credits           INT NOT NULL,
    remaining_credits       INT NOT NULL,
    expires_at              TIMESTAMPTZ NOT NULL,
    status                  VARCHAR(50) NOT NULL DEFAULT 'Active',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_credit_packages_user ON credit_packages(user_id);
CREATE INDEX idx_credit_packages_status ON credit_packages(status);
