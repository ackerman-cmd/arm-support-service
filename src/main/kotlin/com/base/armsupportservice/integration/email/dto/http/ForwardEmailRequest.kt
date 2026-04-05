package com.base.armsupportservice.integration.email.dto.http

import java.util.UUID

data class ForwardEmailRequest(
    val messageId: UUID,
    val to: List<String>,
    val note: String? = null,
    val createdByUserId: UUID? = null,
)
