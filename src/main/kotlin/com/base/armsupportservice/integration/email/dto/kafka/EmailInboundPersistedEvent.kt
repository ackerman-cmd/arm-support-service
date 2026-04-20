package com.base.armsupportservice.integration.email.dto.kafka

import com.fasterxml.jackson.annotation.JsonProperty

/** Топик: email.inbound.persisted */
data class EmailInboundPersistedEvent(
    @JsonProperty("message_id") val messageId: String,
    @JsonProperty("conversation_id") val conversationId: String,
    @JsonProperty("mailbox_id") val mailboxId: String,
    @JsonProperty("case_id") val caseId: String?,
    @JsonProperty("from_email") val fromEmail: String,
    val subject: String?,
    @JsonProperty("text_body") val textBody: String?,
    @JsonProperty("html_body") val htmlBody: String?,
    @JsonProperty("internet_message_id") val internetMessageId: String?,
    @JsonProperty("is_new_conversation") val isNewConversation: Boolean,
    @JsonProperty("received_at") val receivedAt: String,
)
