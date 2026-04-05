package com.base.armsupportservice.integration.email.dto.http

import java.util.UUID

data class SendEmailRequest(
    /** Адрес ящика-отправителя (`support@bank.ru`), как в `Mailbox.emailAddress`. */
    val fromEmail: String,
    val to: List<String>,
    val cc: List<String> = emptyList(),
    val subject: String,
    val htmlBody: String? = null,
    val textBody: String? = null,
    val createdByUserId: UUID? = null,
)
