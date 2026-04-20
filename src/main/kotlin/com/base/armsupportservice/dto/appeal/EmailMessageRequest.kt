package com.base.armsupportservice.dto.appeal

/**
 * Запрос на отправку исходящего сообщения по EMAIL-каналу.
 *
 * [content] — текстовое тело (может быть пустым, если передан [htmlContent]).
 * [fromEmail] — ящик-отправитель; обязателен при первом письме по обращению
 *               (когда у обращения ещё нет emailConversationId).
 * [htmlContent] — HTML-тело письма; при наличии используется вместо [content].
 */
data class EmailMessageRequest(
    val content: String = "",
    val fromEmail: String? = null,
    val htmlContent: String? = null,
)
