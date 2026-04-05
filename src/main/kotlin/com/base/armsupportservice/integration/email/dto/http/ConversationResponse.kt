package com.base.armsupportservice.integration.email.dto.http

import java.util.UUID

data class ConversationResponse(
    val id: UUID,
    val mailboxId: UUID,
    val mailboxEmail: String,
    val subjectNormalized: String? = null,
    val caseId: UUID? = null,
    val status: ConversationStatus,
    val startedAt: String? = null,
    val lastMessageAt: String? = null,
    val createdAt: String,
)
