package com.base.armsupportservice.service

import com.base.armsupportservice.domain.group.AssignmentGroup
import com.base.armsupportservice.dto.group.AssignmentGroupRequest
import com.base.armsupportservice.dto.group.AssignmentGroupResponse
import com.base.armsupportservice.exception.DuplicateResourceException
import com.base.armsupportservice.exception.GroupNotFoundException
import com.base.armsupportservice.exception.OperatorNotFoundException
import com.base.armsupportservice.repository.AssignmentGroupRepository
import com.base.armsupportservice.repository.SyncedUserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AssignmentGroupService(
    private val assignmentGroupRepository: AssignmentGroupRepository,
    private val syncedUserRepository: SyncedUserRepository,
) {
    fun getById(id: UUID): AssignmentGroupResponse {
        val group = assignmentGroupRepository.findById(id).orElseThrow { GroupNotFoundException(id) }
        return toResponse(group)
    }

    fun getAll(pageable: Pageable): Page<AssignmentGroupResponse> = assignmentGroupRepository.findAll(pageable).map { toResponse(it) }

    @Transactional
    fun create(request: AssignmentGroupRequest): AssignmentGroupResponse {
        if (assignmentGroupRepository.existsByName(request.name)) {
            throw DuplicateResourceException("Группа назначения '${request.name}' уже существует")
        }
        validateOperatorsExist(request.operatorIds)
        val group =
            AssignmentGroup(
                name = request.name,
                description = request.description,
                operatorIds = request.operatorIds.toMutableSet(),
            )
        return toResponse(assignmentGroupRepository.save(group))
    }

    @Transactional
    fun update(
        id: UUID,
        request: AssignmentGroupRequest,
    ): AssignmentGroupResponse {
        val group = assignmentGroupRepository.findById(id).orElseThrow { GroupNotFoundException(id) }
        if (assignmentGroupRepository.existsByNameAndIdNot(request.name, id)) {
            throw DuplicateResourceException("Группа назначения '${request.name}' уже существует")
        }
        validateOperatorsExist(request.operatorIds)
        group.name = request.name
        group.description = request.description
        group.operatorIds = request.operatorIds.toMutableSet()
        return toResponse(assignmentGroupRepository.save(group))
    }

    @Transactional
    fun addOperators(
        id: UUID,
        operatorIds: Set<UUID>,
    ): AssignmentGroupResponse {
        val group = assignmentGroupRepository.findById(id).orElseThrow { GroupNotFoundException(id) }
        validateOperatorsExist(operatorIds)
        group.operatorIds.addAll(operatorIds)
        return toResponse(assignmentGroupRepository.save(group))
    }

    @Transactional
    fun removeOperator(
        id: UUID,
        operatorId: UUID,
    ): AssignmentGroupResponse {
        val group = assignmentGroupRepository.findById(id).orElseThrow { GroupNotFoundException(id) }
        group.operatorIds.remove(operatorId)
        return toResponse(assignmentGroupRepository.save(group))
    }

    @Transactional
    fun delete(id: UUID) {
        if (!assignmentGroupRepository.existsById(id)) throw GroupNotFoundException(id)
        assignmentGroupRepository.deleteById(id)
    }

    private fun validateOperatorsExist(operatorIds: Set<UUID>) {
        operatorIds.forEach { opId ->
            if (!syncedUserRepository.existsById(opId)) throw OperatorNotFoundException(opId)
        }
    }

    private fun toResponse(group: AssignmentGroup): AssignmentGroupResponse {
        val operators =
            syncedUserRepository.findAllById(group.operatorIds).map { user ->
                com.base.armsupportservice.dto.common.OperatorSummaryResponse(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    firstName = user.firstName,
                    lastName = user.lastName,
                )
            }
        return AssignmentGroupResponse.from(group, operators)
    }
}
