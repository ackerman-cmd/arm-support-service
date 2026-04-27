package com.base.armsupportservice.integration.llm.dto

import com.fasterxml.jackson.annotation.JsonProperty

/** Топик: llm.appeal.enriched — публикуется llm-service */
data class LlmAppealEnrichedEvent(
    @JsonProperty("conversation_id") val conversationId: String,
    @JsonProperty("priority") val priority: String,
    @JsonProperty("topic_code") val topicCode: String?,
    @JsonProperty("summary") val summary: String,
    @JsonProperty("analyzed_at") val analyzedAt: String,
)
