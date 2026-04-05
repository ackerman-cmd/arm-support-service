ALTER TABLE arm_support.appeals
    ADD COLUMN email_conversation_id UUID;

CREATE UNIQUE INDEX idx_appeals_email_conversation_id
    ON arm_support.appeals (email_conversation_id)
    WHERE email_conversation_id IS NOT NULL;
