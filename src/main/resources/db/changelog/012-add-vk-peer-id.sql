--liquibase formatted sql

--changeset system:012-add-vk-peer-id
ALTER TABLE arm_support.appeals
    ADD COLUMN vk_peer_id BIGINT;

CREATE INDEX idx_appeals_vk_peer_id
    ON arm_support.appeals (vk_peer_id)
    WHERE vk_peer_id IS NOT NULL;

--rollback ALTER TABLE arm_support.appeals DROP COLUMN vk_peer_id;
