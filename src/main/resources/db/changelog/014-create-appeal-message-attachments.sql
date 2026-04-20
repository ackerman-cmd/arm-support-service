--liquibase formatted sql
--changeset system:014-create-appeal-message-attachments

CREATE TABLE arm_support.appeal_message_attachments
(
    id              UUID         NOT NULL PRIMARY KEY,
    message_id      UUID         NOT NULL REFERENCES arm_support.appeal_messages (id) ON DELETE CASCADE,
    attachment_type VARCHAR(50)  NOT NULL,
    file_name       TEXT         NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    s3_key          TEXT         NOT NULL,
    s3_url          TEXT         NOT NULL,
    file_size       BIGINT,
    created_at      TIMESTAMP    NOT NULL
);

CREATE INDEX idx_appeal_msg_attachments_message ON arm_support.appeal_message_attachments (message_id);

--rollback DROP TABLE arm_support.appeal_message_attachments;
