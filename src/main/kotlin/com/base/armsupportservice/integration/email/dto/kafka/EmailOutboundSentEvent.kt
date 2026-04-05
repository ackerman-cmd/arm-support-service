package com.base.armsupportservice.integration.email.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty

/** Топик: email.outbound.sent */
data class EmailOutboundSentEvent(
    @JsonProperty("message_id") val messageId: String,
    @JsonProperty("conversation_id") val conversationId: String,
    @JsonProperty("case_id") val caseId: String?,
    @JsonProperty("provider_message_id") val providerMessageId: String?,
    @JsonProperty("to_emails") val toEmails: List<String>,
    val subject: String?,
    @JsonProperty("sent_at") val sentAt: String,
)
