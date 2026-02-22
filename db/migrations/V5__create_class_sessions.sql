CREATE TABLE class_sessions (
  id            UUID        PRIMARY KEY,
  class_type_id UUID        NOT NULL REFERENCES class_types(id),
  instructor_id UUID        NOT NULL REFERENCES instructors(id),
  studio_id     UUID        NOT NULL REFERENCES studios(id),
  start_time    TIMESTAMPTZ NOT NULL,
  max_capacity  INTEGER     NOT NULL,
  booked_count  INTEGER     NOT NULL DEFAULT 0,
  status        VARCHAR(20) NOT NULL DEFAULT 'Scheduled',
  created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_class_sessions_start_time ON class_sessions(start_time);
CREATE INDEX idx_class_sessions_instructor ON class_sessions(instructor_id);
