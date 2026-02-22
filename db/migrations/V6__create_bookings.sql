CREATE TABLE bookings (
  id               UUID        PRIMARY KEY,
  member_id        UUID        NOT NULL REFERENCES members(id),
  class_session_id UUID        NOT NULL REFERENCES class_sessions(id),
  status           VARCHAR(20) NOT NULL DEFAULT 'Confirmed',
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  cancelled_at     TIMESTAMPTZ
);

CREATE INDEX idx_bookings_member ON bookings(member_id);
CREATE INDEX idx_bookings_session ON bookings(class_session_id);
CREATE UNIQUE INDEX idx_bookings_active_member_session
  ON bookings(member_id, class_session_id)
  WHERE status IN ('Confirmed', 'WaitListed');
