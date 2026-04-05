package com.base.armsupportservice.dto.appeal

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealPriority
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.dto.common.OperatorSummaryResponse
import com.base.armsupportservice.dto.group.AssignmentGroupSummaryResponse
import com.base.armsupportservice.dto.group.SkillGroupSummaryResponse
import com.base.armsupportservice.dto.organization.OrganizationSummaryResponse
import com.base.armsupportservice.dto.topic.AppealTopicSummaryResponse
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.UUID

data class AppealResponse(
    val id: UUID,
    val subject: String,
    val description: String?,
    val channel: AppealChannel,
    val direction: AppealDirection,
    val status: AppealStatus,
    val priority: AppealPriority,
    val topic: AppealTopicSummaryResponse?,
    val organization: OrganizationSummaryResponse?,
    val contactName: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    /** Ответственный оператор (при прямом назначении) */
    val assignedOperator: OperatorSummaryResponse?,
    /** Все операторы, активно работающие с обращением в данный момент */
    val activeOperators: List<OperatorSummaryResponse>,
    val assignmentGroup: AssignmentGroupSummaryResponse?,
    val skillGroup: SkillGroupSummaryResponse?,
    val createdById: UUID,
    val emailConversationId: UUID?,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val closedAt: LocalDateTime?,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val createdAt: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(
            appeal: Appeal,
            assignedOperator: OperatorSummaryResponse?,
            activeOperators: List<OperatorSummaryResponse> = emptyList(),
        ) = AppealResponse(
            id = appeal.id,
            subject = appeal.subject,
            description = appeal.description,
            channel = appeal.channel,
            direction = appeal.direction,
            status = appeal.status,
            priority = appeal.priority,
            topic = appeal.topic?.let { AppealTopicSummaryResponse.from(it) },
            organization = appeal.organization?.let { OrganizationSummaryResponse.from(it) },
            contactName = appeal.contactName,
            contactEmail = appeal.contactEmail,
            contactPhone = appeal.contactPhone,
            assignedOperator = assignedOperator,
            activeOperators = activeOperators,
            assignmentGroup = appeal.assignmentGroup?.let { AssignmentGroupSummaryResponse.from(it) },
            skillGroup = appeal.skillGroup?.let { SkillGroupSummaryResponse.from(it) },
            createdById = appeal.createdById,
            emailConversationId = appeal.emailConversationId,
            closedAt = appeal.closedAt,
            createdAt = appeal.createdAt,
            updatedAt = appeal.updatedAt,
        )
    }
}
