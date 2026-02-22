CREATE TABLE class_types (
  id               UUID PRIMARY KEY,
  name             VARCHAR(200) NOT NULL,
  description      TEXT         NOT NULL,
  duration_minutes INTEGER      NOT NULL,
  difficulty_level VARCHAR(20)  NOT NULL,
  max_capacity     INTEGER      NOT NULL,
  price_in_cents   BIGINT       NOT NULL
);
