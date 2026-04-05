package com.base.armsupportservice.integration.email.dto.http

import java.util.UUID

data class AttachmentResponse(
    val id: UUID,
    val messageId: UUID,
    val fileName: String,
    val contentType: String,
    val sizeBytes: Long? = null,
    val isInline: Boolean,
    val contentId: String? = null,
)
