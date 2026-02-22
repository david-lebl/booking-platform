CREATE TABLE instructors (
  id               UUID PRIMARY KEY,
  first_name       VARCHAR(100) NOT NULL,
  last_name        VARCHAR(100) NOT NULL,
  email            VARCHAR(255) NOT NULL UNIQUE,
  phone            VARCHAR(50),
  bio              TEXT,
  specializations  TEXT[]       NOT NULL DEFAULT '{}',
  active           BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
