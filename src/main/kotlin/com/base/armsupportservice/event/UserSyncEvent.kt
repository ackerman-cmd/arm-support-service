package com.base.armsupportservice.event

import com.base.armsupportservice.domain.user.UserStatus
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.UUID

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
