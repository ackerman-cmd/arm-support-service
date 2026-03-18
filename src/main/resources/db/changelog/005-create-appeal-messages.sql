CREATE TABLE arm_support.appeal_messages
(
    id                  UUID        NOT NULL PRIMARY KEY,
    appeal_id           UUID        NOT NULL REFERENCES arm_support.appeals (id) ON DELETE CASCADE,
    sender_id           UUID,
    sender_type         VARCHAR(32) NOT NULL,
    content             TEXT        NOT NULL,
    channel             VARCHAR(32) NOT NULL,
    external_message_id VARCHAR(512),
    created_at          TIMESTAMP   NOT NULL
);

CREATE INDEX idx_appeal_messages_appeal_id ON arm_support.appeal_messages (appeal_id);
CREATE INDEX idx_appeal_messages_created_at ON arm_support.appeal_messages (created_at);
CREATE UNIQUE INDEX idx_appeal_messages_external_id
    ON arm_support.appeal_messages (external_message_id)
    WHERE external_message_id IS NOT NULL;
