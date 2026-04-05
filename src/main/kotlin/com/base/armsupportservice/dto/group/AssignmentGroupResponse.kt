package com.base.armsupportservice.dto.group

import com.base.armsupportservice.domain.group.AssignmentGroup
import com.base.armsupportservice.dto.common.OperatorSummaryResponse
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.UUID

data class AssignmentGroupResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val mailboxEmail: String?,
    val operators: List<OperatorSummaryResponse>,
    val operatorCount: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val createdAt: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(
            group: AssignmentGroup,
            operators: List<OperatorSummaryResponse>,
        ) = AssignmentGroupResponse(
            id = group.id,
            name = group.name,
            description = group.description,
            mailboxEmail = group.mailboxEmail,
            operators = operators,
            operatorCount = operators.size,
            createdAt = group.createdAt,
            updatedAt = group.updatedAt,
        )
    }
}

data class AssignmentGroupSummaryResponse(
    val id: UUID,
    val name: String,
    val mailboxEmail: String?,
    val operatorCount: Int,
) {
    companion object {
        fun from(group: AssignmentGroup) =
            AssignmentGroupSummaryResponse(
                id = group.id,
                name = group.name,
                mailboxEmail = group.mailboxEmail,
                operatorCount = group.operatorIds.size,
            )
    }
}
