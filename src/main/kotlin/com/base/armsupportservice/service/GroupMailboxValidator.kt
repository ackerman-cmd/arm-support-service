package com.base.armsupportservice.service

import com.base.armsupportservice.exception.DuplicateResourceException
import com.base.armsupportservice.repository.AssignmentGroupRepository
import com.base.armsupportservice.repository.SkillGroupRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GroupMailboxValidator(
    private val assignmentGroupRepository: AssignmentGroupRepository,
    private val skillGroupRepository: SkillGroupRepository,
) {
    fun normalize(mailboxEmail: String?): String? = mailboxEmail?.trim()?.takeIf { it.isNotEmpty() }?.lowercase()

    fun validateForAssignmentGroup(
        mailboxEmail: String?,
        groupId: UUID?,
    ) {
        val normalized = normalize(mailboxEmail) ?: return
        assignmentGroupRepository.findByMailboxEmail(normalized)?.let { existing ->
            if (groupId == null || existing.id != groupId) {
                throw DuplicateResourceException("Почтовый ящик '$normalized' уже привязан к другой группе назначения")
            }
        }
        skillGroupRepository.findByMailboxEmail(normalized)?.let {
            throw DuplicateResourceException("Почтовый ящик '$normalized' уже привязан к скилл-группе")
        }
    }

    fun validateForSkillGroup(
        mailboxEmail: String?,
        groupId: UUID?,
    ) {
        val normalized = normalize(mailboxEmail) ?: return
        skillGroupRepository.findByMailboxEmail(normalized)?.let { existing ->
            if (groupId == null || existing.id != groupId) {
                throw DuplicateResourceException("Почтовый ящик '$normalized' уже привязан к другой скилл-группе")
            }
        }
        assignmentGroupRepository.findByMailboxEmail(normalized)?.let {
            throw DuplicateResourceException("Почтовый ящик '$normalized' уже привязан к группе назначения")
        }
    }
}
