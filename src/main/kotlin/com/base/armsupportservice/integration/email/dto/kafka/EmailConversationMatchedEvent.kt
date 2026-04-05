package com.base.armsupportservice.integration.email.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty

/** Топик: email.conversation.matched */
data class EmailConversationMatchedEvent(
    @JsonProperty("message_id") val messageId: String,
    @JsonProperty("conversation_id") val conversationId: String,
    @JsonProperty("case_id") val caseId: String?,
    @JsonProperty("matched_by") val matchedBy: String?,
    @JsonProperty("matched_at") val matchedAt: String,
)
