package com.base.armsupportservice.integration.email.dto.http

import java.util.UUID

data class MessageCreatedResponse(
    val messageId: UUID,
    val conversationId: UUID,
    val status: MessageStatus,
)
