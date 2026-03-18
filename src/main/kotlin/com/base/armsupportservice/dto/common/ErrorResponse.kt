package com.base.armsupportservice.dto.common

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val details: Map<String, String> = emptyMap(),
)
