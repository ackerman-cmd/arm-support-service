package com.base.armsupportservice.integration.email.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty

/** Топик: email.outbound.failed */
data class EmailOutboundFailedEvent(
    @JsonProperty("message_id") val messageId: String,
    @JsonProperty("conversation_id") val conversationId: String,
    @JsonProperty("case_id") val caseId: String?,
    val reason: String,
    @JsonProperty("failed_at") val failedAt: String,
)
