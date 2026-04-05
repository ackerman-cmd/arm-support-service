package com.base.armsupportservice.integration.email.dto.http

import java.util.UUID

data class MessageResponse(
    val id: UUID,
    val conversationId: UUID,
    val direction: MessageDirection,
    val status: MessageStatus,
    val providerMessageId: String? = null,
    val internetMessageId: String? = null,
    val inReplyTo: String? = null,
    val subject: String? = null,
    val fromEmail: String,
    val fromName: String? = null,
    val replyToEmail: String? = null,
    val textBody: String? = null,
    val htmlBody: String? = null,
    val sentAt: String? = null,
    val receivedAt: String? = null,
    val createdAt: String,
    val recipients: List<RecipientResponse> = emptyList(),
)
