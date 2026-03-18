package com.base.armsupportservice.event

import com.base.armsupportservice.domain.user.UserStatus
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.UUID

/**
 * Контракт сообщения из топика user-sync (сервис-отправитель).
 * Ключ сообщения: aggregateId = userId (UUID строка).
 * Заголовок: __TypeId__ = имя типа события (USER_REGISTERED, USER_EMAIL_VERIFIED, ...).
 * Тело: полный снимок пользователя + eventType + timestamp (date-time).
 */
data class UserSyncEvent(
    val userId: UUID,
    val email: String,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val status: UserStatus,
    val emailVerified: Boolean,
    val roles: List<String>,
    val eventType: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val timestamp: LocalDateTime,
)
