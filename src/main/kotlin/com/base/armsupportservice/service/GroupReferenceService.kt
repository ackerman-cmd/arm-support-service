package com.base.armsupportservice.service

import com.base.armsupportservice.dto.common.OperatorSummaryResponse
import com.base.armsupportservice.dto.group.AllGroupsWithOperatorsResponse
import com.base.armsupportservice.dto.group.GroupWithOperatorsDto
import com.base.armsupportservice.repository.AssignmentGroupRepository
import com.base.armsupportservice.repository.SkillGroupRepository
import com.base.armsupportservice.repository.SyncedUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GroupReferenceService(
    private val assignmentGroupRepository: AssignmentGroupRepository,
    private val skillGroupRepository: SkillGroupRepository,
    private val syncedUserRepository: SyncedUserRepository,
) {
    fun getAllGroupsWithOperators(): AllGroupsWithOperatorsResponse {
        val assignmentGroups = assignmentGroupRepository.findAll()
        val skillGroups = skillGroupRepository.findAll()

        val allOperatorIds =
            assignmentGroups.flatMap { it.operatorIds }.toSet() +
                skillGroups.flatMap { it.operatorIds }.toSet()

        val usersById =
            syncedUserRepository.findAllById(allOperatorIds).associate { user ->
                user.id to
                    OperatorSummaryResponse(
                        id = user.id,
                        username = user.username,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                    )
            }

        return AllGroupsWithOperatorsResponse(
            assignmentGroups =
                assignmentGroups.map { group ->
                    GroupWithOperatorsDto(
                        id = group.id,
                        name = group.name,
                        operators = group.operatorIds.mapNotNull { usersById[it] },
                    )
                },
            skillGroups =
                skillGroups.map { group ->
                    GroupWithOperatorsDto(
                        id = group.id,
                        name = group.name,
                        operators = group.operatorIds.mapNotNull { usersById[it] },
                    )
                },
        )
    }
}
