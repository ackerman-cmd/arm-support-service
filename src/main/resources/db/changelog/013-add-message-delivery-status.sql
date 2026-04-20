--liquibase formatted sql
--changeset system:013-add-message-delivery-status

ALTER TABLE arm_support.appeal_messages
    ADD COLUMN delivery_status VARCHAR(32);

CREATE INDEX idx_appeal_messages_delivery_status
    ON arm_support.appeal_messages (delivery_status)
    WHERE delivery_status IS NOT NULL;

--rollback ALTER TABLE arm_support.appeal_messages DROP COLUMN delivery_status;
