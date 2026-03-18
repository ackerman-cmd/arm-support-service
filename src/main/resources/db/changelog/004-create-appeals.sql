CREATE TABLE arm_support.appeals
(
    id                   UUID         NOT NULL PRIMARY KEY,
    subject              VARCHAR(512) NOT NULL,
    description          TEXT,
    channel              VARCHAR(32)  NOT NULL,
    direction            VARCHAR(32)  NOT NULL,
    status               VARCHAR(64)  NOT NULL,
    priority             VARCHAR(32)  NOT NULL DEFAULT 'MEDIUM',
    organization_id      UUID REFERENCES arm_support.organizations (id) ON DELETE SET NULL,
    contact_name         VARCHAR(255),
    contact_email        VARCHAR(255),
    contact_phone        VARCHAR(32),
    assigned_operator_id UUID,
    assignment_group_id  UUID REFERENCES arm_support.assignment_groups (id) ON DELETE SET NULL,
    skill_group_id       UUID REFERENCES arm_support.skill_groups (id) ON DELETE SET NULL,
    created_by_id        UUID         NOT NULL,
    closed_at            TIMESTAMP,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL
);

CREATE INDEX idx_appeals_status ON arm_support.appeals (status);
CREATE INDEX idx_appeals_channel ON arm_support.appeals (channel);
CREATE INDEX idx_appeals_direction ON arm_support.appeals (direction);
CREATE INDEX idx_appeals_priority ON arm_support.appeals (priority);
CREATE INDEX idx_appeals_organization_id ON arm_support.appeals (organization_id);
CREATE INDEX idx_appeals_assigned_operator_id ON arm_support.appeals (assigned_operator_id);
CREATE INDEX idx_appeals_assignment_group_id ON arm_support.appeals (assignment_group_id);
CREATE INDEX idx_appeals_skill_group_id ON arm_support.appeals (skill_group_id);
CREATE INDEX idx_appeals_created_by_id ON arm_support.appeals (created_by_id);
CREATE INDEX idx_appeals_created_at ON arm_support.appeals (created_at DESC);
