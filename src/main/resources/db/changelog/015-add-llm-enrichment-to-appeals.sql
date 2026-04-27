--liquibase formatted sql

--changeset arm-support:015-add-llm-enrichment-to-appeals
ALTER TABLE arm_support.appeals
    ADD COLUMN IF NOT EXISTS summary TEXT;

COMMENT ON COLUMN arm_support.appeals.summary IS 'Краткое резюме обращения, сгенерированное LLM-сервисом';
