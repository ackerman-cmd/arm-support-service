package com.base.armsupportservice.dto.group

import com.base.armsupportservice.domain.group.SkillGroup
import com.base.armsupportservice.dto.common.OperatorSummaryResponse
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.UUID

data class SkillGroupResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val mailboxEmail: String?,
    val skills: Set<String>,
    val operators: List<OperatorSummaryResponse>,
    val operatorCount: Int,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val createdAt: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(
            group: SkillGroup,
            operators: List<OperatorSummaryResponse>,
        ) = SkillGroupResponse(
            id = group.id,
            name = group.name,
            description = group.description,
            mailboxEmail = group.mailboxEmail,
            skills = group.skills,
            operators = operators,
            operatorCount = operators.size,
            createdAt = group.createdAt,
            updatedAt = group.updatedAt,
        )
    }
}

data class SkillGroupSummaryResponse(
    val id: UUID,
    val name: String,
    val mailboxEmail: String?,
    val skills: Set<String>,
    val operatorCount: Int,
) {
    companion object {
        fun from(group: SkillGroup) =
            SkillGroupSummaryResponse(
                id = group.id,
                name = group.name,
                mailboxEmail = group.mailboxEmail,
                skills = group.skills,
                operatorCount = group.operatorIds.size,
            )
    }
}
