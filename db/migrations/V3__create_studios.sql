CREATE TABLE studios (
  id          UUID PRIMARY KEY,
  name        VARCHAR(200) NOT NULL,
  street      VARCHAR(200) NOT NULL,
  city        VARCHAR(100) NOT NULL,
  country     VARCHAR(100) NOT NULL,
  postal_code VARCHAR(20)  NOT NULL,
  capacity    INTEGER      NOT NULL,
  active      BOOLEAN      NOT NULL DEFAULT TRUE
);
