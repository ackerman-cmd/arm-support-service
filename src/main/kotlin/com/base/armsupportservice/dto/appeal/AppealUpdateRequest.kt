package com.base.armsupportservice.dto.appeal

import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealPriority
import jakarta.validation.constraints.Size
import java.util.UUID

data class AppealUpdateRequest(
    @field:Size(max = 512)
    val subject: String? = null,
    val description: String? = null,
    val channel: AppealChannel? = null,
    val priority: AppealPriority? = null,
    val organizationId: UUID? = null,
    val topicId: UUID? = null,
    val contactName: String? = null,
    val contactEmail: String? = null,
    @field:Size(max = 32)
    val contactPhone: String? = null,
    val assignmentGroupId: UUID? = null,
    val skillGroupId: UUID? = null,
)
