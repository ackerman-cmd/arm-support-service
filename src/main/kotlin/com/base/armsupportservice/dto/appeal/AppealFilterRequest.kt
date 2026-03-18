package com.base.armsupportservice.dto.appeal

import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealPriority
import com.base.armsupportservice.domain.appeal.AppealStatus
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import java.util.UUID

data class AppealFilterRequest(
    val status: AppealStatus? = null,
    val channel: AppealChannel? = null,
    val direction: AppealDirection? = null,
    val priority: AppealPriority? = null,
    val organizationId: UUID? = null,
    val assignedOperatorId: UUID? = null,
    val assignmentGroupId: UUID? = null,
    val skillGroupId: UUID? = null,
    val createdById: UUID? = null,
    val subject: String? = null,
    val contactEmail: String? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val createdFrom: LocalDateTime? = null,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val createdTo: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC",
)
