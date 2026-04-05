package com.base.armsupportservice.integration.email.dto.http

import java.util.UUID

data class ReplyEmailRequest(
    val conversationId: UUID,
    val replyToMessageId: UUID? = null,
    val to: List<String>,
    val cc: List<String> = emptyList(),
    val htmlBody: String? = null,
    val textBody: String? = null,
    val createdByUserId: UUID? = null,
)
