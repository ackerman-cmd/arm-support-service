package com.base.armsupportservice.integration.email.dto.http

data class RecipientResponse(
    val type: RecipientType,
    val email: String,
    val name: String? = null,
)
