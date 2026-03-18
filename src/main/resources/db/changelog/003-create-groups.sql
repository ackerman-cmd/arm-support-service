CREATE TABLE arm_support.assignment_groups
(
    id          UUID         NOT NULL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_assignment_groups_name ON arm_support.assignment_groups (name);

CREATE TABLE arm_support.assignment_group_operators
(
    group_id    UUID NOT NULL REFERENCES arm_support.assignment_groups (id) ON DELETE CASCADE,
    operator_id UUID NOT NULL,
    PRIMARY KEY (group_id, operator_id)
);

CREATE INDEX idx_assignment_group_operators_operator ON arm_support.assignment_group_operators (operator_id);

-- -----------------------------------------------------------------------

CREATE TABLE arm_support.skill_groups
(
    id          UUID         NOT NULL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_skill_groups_name ON arm_support.skill_groups (name);

CREATE TABLE arm_support.skill_group_skills
(
    group_id UUID         NOT NULL REFERENCES arm_support.skill_groups (id) ON DELETE CASCADE,
    skill    VARCHAR(255) NOT NULL,
    PRIMARY KEY (group_id, skill)
);

CREATE TABLE arm_support.skill_group_operators
(
    group_id    UUID NOT NULL REFERENCES arm_support.skill_groups (id) ON DELETE CASCADE,
    operator_id UUID NOT NULL,
    PRIMARY KEY (group_id, operator_id)
);

CREATE INDEX idx_skill_group_operators_operator ON arm_support.skill_group_operators (operator_id);
