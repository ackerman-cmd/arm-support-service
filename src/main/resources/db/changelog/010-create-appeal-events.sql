CREATE TABLE arm_support.appeal_events
(
    id           UUID        NOT NULL PRIMARY KEY,
    appeal_id    UUID        NOT NULL REFERENCES arm_support.appeals (id) ON DELETE CASCADE,
    event_type   VARCHAR(64) NOT NULL,
    initiator_id UUID,
    from_status  VARCHAR(64),
    to_status    VARCHAR(64),
    comment      VARCHAR(512),
    created_at   TIMESTAMP   NOT NULL
);

CREATE INDEX idx_appeal_events_appeal_id  ON arm_support.appeal_events (appeal_id);
CREATE INDEX idx_appeal_events_created_at ON arm_support.appeal_events (created_at);
