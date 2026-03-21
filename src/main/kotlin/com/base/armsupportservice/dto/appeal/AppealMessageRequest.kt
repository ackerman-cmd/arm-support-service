package com.base.armsupportservice.dto.appeal

import com.base.armsupportservice.domain.appeal.AppealChannel
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class AppealMessageRequest(
    @field:NotBlank(message = "Содержимое сообщения обязательно")
    val content: String,
    @field:NotNull(message = "Канал отправки обязателен")
    val channel: AppealChannel,
    val externalMessageId: String? = null,
)
