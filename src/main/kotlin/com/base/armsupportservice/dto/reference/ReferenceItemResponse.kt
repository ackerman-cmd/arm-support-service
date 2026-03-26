package com.base.armsupportservice.dto.reference

data class ReferenceItemResponse(
    val code: String,
    val label: String,
    val description: String? = null,
)
