package com.base.armsupportservice.dto.group

import com.base.armsupportservice.dto.common.OperatorSummaryResponse
import java.util.UUID

data class GroupWithOperatorsDto(
    val id: UUID,
    val name: String,
    val mailboxEmail: String?,
    val operators: List<OperatorSummaryResponse>,
)

data class AllGroupsWithOperatorsResponse(
    val assignmentGroups: List<GroupWithOperatorsDto>,
    val skillGroups: List<GroupWithOperatorsDto>,
)
