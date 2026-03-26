CREATE TABLE arm_support.appeal_active_operators
(
    appeal_id   UUID      NOT NULL REFERENCES arm_support.appeals (id) ON DELETE CASCADE,
    operator_id UUID      NOT NULL,
    joined_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (appeal_id, operator_id)
);

CREATE INDEX idx_appeal_active_operators_operator ON arm_support.appeal_active_operators (operator_id);
