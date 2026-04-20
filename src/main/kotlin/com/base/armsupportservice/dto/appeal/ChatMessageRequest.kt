package com.base.armsupportservice.dto.appeal

import jakarta.validation.constraints.NotBlank

/** Запрос на отправку исходящего сообщения по CHAT (VK)-каналу. */
data class ChatMessageRequest(
    @field:NotBlank(message = "Содержимое сообщения обязательно")
    val content: String,
)
