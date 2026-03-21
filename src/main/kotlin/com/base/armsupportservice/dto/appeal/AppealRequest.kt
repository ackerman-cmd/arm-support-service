package com.base.armsupportservice.dto.appeal

import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealPriority
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

data class AppealRequest(
    @field:NotBlank(message = "Тема обращения обязательна")
    @field:Size(max = 512)
    val subject: String,
    val description: String? = null,
    @field:NotNull(message = "Канал обращения обязателен")
    val channel: AppealChannel,
    @field:NotNull(message = "Направление обращения обязательно")
    val direction: AppealDirection,
    val priority: AppealPriority = AppealPriority.MEDIUM,
    val organizationId: UUID? = null,
    val contactName: String? = null,
    val contactEmail: String? = null,
    @field:Size(max = 32)
    val contactPhone: String? = null,
    val assignedOperatorId: UUID? = null,
    val assignmentGroupId: UUID? = null,
    val skillGroupId: UUID? = null,
)
