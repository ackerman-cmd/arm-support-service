package com.base.armsupportservice.service

import com.base.armsupportservice.dto.mailbox.ActiveMailboxResponse
import com.base.armsupportservice.exception.GroupNotFoundException
import com.base.armsupportservice.exception.MailboxNotConfiguredException
import com.base.armsupportservice.repository.AssignmentGroupRepository
import com.base.armsupportservice.repository.SkillGroupRepository
import com.base.armsupportservice.repository.SyncedUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ActiveMailboxService(
    private val assignmentGroupRepository: AssignmentGroupRepository,
    private val skillGroupRepository: SkillGroupRepository,
    private val syncedUserRepository: SyncedUserRepository,
) {
    fun getByUser(username: String): List<ActiveMailboxResponse> {
        val user = syncedUserRepository.findByUsername(username) ?: return emptyList()
        val assignmentGroups = assignmentGroupRepository.findAllByOperatorId(user.id)
        val skillGroups = skillGroupRepository.findAllByOperatorId(user.id)

        val assignment =
            assignmentGroups
                .mapNotNull { group ->
                    group.mailboxEmail?.let { email ->
                        ActiveMailboxResponse(
                            groupId = group.id,
                            groupType = "ASSIGNMENT",
                            groupName = group.name,
                            email = email,
                        )
                    }
                }

        val skill =
            skillGroups
                .mapNotNull { group ->
                    group.mailboxEmail?.let { email ->
                        ActiveMailboxResponse(
                            groupId = group.id,
                            groupType = "SKILL",
                            groupName = group.name,
                            email = email,
                        )
                    }
                }

        return (assignment + skill).sortedBy { it.groupName }
    }

    fun getByAssignmentGroup(groupId: UUID): ActiveMailboxResponse {
        val group = assignmentGroupRepository.findById(groupId).orElseThrow { GroupNotFoundException(groupId) }
        val mailbox = group.mailboxEmail ?: throw MailboxNotConfiguredException(groupId)
        return ActiveMailboxResponse(
            groupId = group.id,
            groupType = "ASSIGNMENT",
            groupName = group.name,
            email = mailbox,
        )
    }
}
