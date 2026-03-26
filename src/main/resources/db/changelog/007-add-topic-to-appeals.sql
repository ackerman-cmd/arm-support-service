ALTER TABLE arm_support.appeals
    ADD COLUMN topic_id UUID REFERENCES arm_support.appeal_topics (id) ON DELETE SET NULL;

CREATE INDEX idx_appeals_topic_id ON arm_support.appeals (topic_id);
