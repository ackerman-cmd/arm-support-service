package com.base.armsupportservice.service

import com.base.armsupportservice.domain.group.SkillGroup
import com.base.armsupportservice.dto.common.OperatorSummaryResponse
import com.base.armsupportservice.dto.group.SkillGroupRequest
import com.base.armsupportservice.dto.group.SkillGroupResponse
import com.base.armsupportservice.exception.DuplicateResourceException
import com.base.armsupportservice.exception.GroupNotFoundException
import com.base.armsupportservice.exception.OperatorNotFoundException
import com.base.armsupportservice.repository.SkillGroupRepository
import com.base.armsupportservice.repository.SyncedUserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class SkillGroupService(
    private val skillGroupRepository: SkillGroupRepository,
    private val syncedUserRepository: SyncedUserRepository,
    private val groupMailboxValidator: GroupMailboxValidator,
) {
    fun getById(id: UUID): SkillGroupResponse {
        val group = skillGroupRepository.findById(id).orElseThrow { GroupNotFoundException(id) }
        return toResponse(group)
    }

    fun getAll(pageable: Pageable): Page<SkillGroupResponse> = skillGroupRepository.findAll(pageable).map { toResponse(it) }

    @Transactional
    fun create(request: SkillGroupRequest): SkillGroupResponse {
        if (skillGroupRepository.existsByName(request.name)) {
            throw DuplicateResourceException("Скилл-группа '${request.name}' уже существует")
        }
        groupMailboxValidator.validateForSkillGroup(request.mailboxEmail, null)
        validateOperatorsExist(request.operatorIds)
        val group =
            SkillGroup(
                name = request.name,
                description = request.description,
                mailboxEmail = groupMailboxValidator.normalize(request.mailboxEmail),
                skills = request.skills.toMutableSet(),
                operatorIds = request.operatorIds.toMutableSet(),
            )
        return toResponse(skillGroupRepository.save(group))
    }

    @Transactional
    fun update(
        id: UUID,
        request: SkillGroupRequest,
    ): SkillGroupResponse {
        val group = skillGroupRepository.findById(id).orElseThrow { GroupNotFoundException(id) }
        if (skillGroupRepository.existsByNameAndIdNot(request.name, id)) {
            throw DuplicateResourceException("Скилл-группа '${request.name}' уже существует")
        }
        groupMailboxValidator.validateForSkillGroup(request.mailboxEmail, id)
        validateOperatorsExist(request.operatorIds)
        group.name = request.name
        group.description = request.description
        group.mailboxEmail = groupMailboxValidator.normalize(request.mailboxEmail)
        group.skills = request.skills.toMutableSet()
        group.operatorIds = request.operatorIds.toMutableSet()
        return toResponse(skillGroupRepository.save(group))
    }

    @Transactional
    fun addOperators(
        id: UUID,
        operatorIds: Set<UUID>,
    ): SkillGroupResponse {
        val group = skillGroupRepository.findById(id).orElseThrow { GroupNotFoundException(id) }
        validateOperatorsExist(operatorIds)
        group.operatorIds.addAll(operatorIds)
        return toResponse(skillGroupRepository.save(group))
    }

    @Transactional
    fun removeOperator(
        id: UUID,
        operatorId: UUID,
    ): SkillGroupResponse {
        val group = skillGroupRepository.findById(id).orElseThrow { GroupNotFoundException(id) }
        group.operatorIds.remove(operatorId)
        return toResponse(skillGroupRepository.save(group))
    }

    fun fetchByIds(ids: Set<UUID>): List<SkillGroupResponse> = skillGroupRepository.findAllById(ids).map { toResponse(it) }

    @Transactional
    fun delete(id: UUID) {
        if (!skillGroupRepository.existsById(id)) throw GroupNotFoundException(id)
        skillGroupRepository.deleteById(id)
    }

    private fun validateOperatorsExist(operatorIds: Set<UUID>) {
        operatorIds.forEach { opId ->
            if (!syncedUserRepository.existsById(opId)) throw OperatorNotFoundException(opId)
        }
    }

    private fun toResponse(group: SkillGroup): SkillGroupResponse {
        val operators =
            syncedUserRepository.findAllById(group.operatorIds).map { user ->
                OperatorSummaryResponse(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    firstName = user.firstName,
                    lastName = user.lastName,
                )
            }
        return SkillGroupResponse.from(group, operators)
    }
}
