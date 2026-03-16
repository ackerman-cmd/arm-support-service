-- liquibase formatted sql

-- changeset arm-support:001-create-synced-users

CREATE TABLE arm_support.synced_users
(
    id             UUID         NOT NULL PRIMARY KEY,
    email          VARCHAR(255) NOT NULL,
    username       VARCHAR(64)  NOT NULL,
    first_name     VARCHAR(64),
    last_name      VARCHAR(64),
    status         VARCHAR(64)  NOT NULL,
    email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    roles          TEXT[]       NOT NULL DEFAULT '{}',
    synced_at      TIMESTAMP    NOT NULL,
    created_at     TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_synced_users_email ON arm_support.synced_users (email);
CREATE UNIQUE INDEX idx_synced_users_username ON arm_support.synced_users (username);
CREATE INDEX idx_synced_users_status ON arm_support.synced_users (status);
