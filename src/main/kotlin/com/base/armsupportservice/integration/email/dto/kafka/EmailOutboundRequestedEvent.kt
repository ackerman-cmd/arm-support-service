package com.base.armsupportservice.integration.email.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty

/** Топик: email.outbound.requested */
data class EmailOutboundRequestedEvent(
    @JsonProperty("message_id") val messageId: String,
    @JsonProperty("conversation_id") val conversationId: String,
    @JsonProperty("created_by_user_id") val createdByUserId: String?,
    @JsonProperty("to_emails") val toEmails: List<String>,
    val subject: String?,
    @JsonProperty("requested_at") val requestedAt: String,
)
