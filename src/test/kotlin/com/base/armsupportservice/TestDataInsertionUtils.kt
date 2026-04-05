package com.base.armsupportservice

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealPriority
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.domain.group.AssignmentGroup
import com.base.armsupportservice.domain.group.SkillGroup
import com.base.armsupportservice.domain.organization.Organization
import com.base.armsupportservice.domain.user.SyncedUser
import com.base.armsupportservice.domain.user.UserStatus
import com.base.armsupportservice.repository.AppealRepository
import com.base.armsupportservice.repository.AssignmentGroupRepository
import com.base.armsupportservice.repository.OrganizationRepository
import com.base.armsupportservice.repository.SkillGroupRepository
import com.base.armsupportservice.repository.SyncedUserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

@Component
class TestDataInsertionUtils(
    private val syncedUserRepository: SyncedUserRepository,
    private val organizationRepository: OrganizationRepository,
    private val assignmentGroupRepository: AssignmentGroupRepository,
    private val skillGroupRepository: SkillGroupRepository,
    private val appealRepository: AppealRepository,
) {
    @Transactional
    fun insertSyncedUser(
        id: UUID = UUID.randomUUID(),
        email: String = "user${UUID.randomUUID()}@example.com",
        username: String = "testuser${UUID.randomUUID()}",
        firstName: String? = "John",
        lastName: String? = "Doe",
        status: UserStatus = UserStatus.ACTIVE,
        emailVerified: Boolean = true,
        roles: List<String> = listOf("ROLE_USER"),
    ): SyncedUser {
        val now = LocalDateTime.now()
        return syncedUserRepository.save(
            SyncedUser(
                id = id,
                email = email,
                username = username,
                firstName = firstName,
                lastName = lastName,
                status = status,
                emailVerified = emailVerified,
                roles = roles,
                syncedAt = now,
                createdAt = now,
            ),
        )
    }

    @Transactional
    fun insertOrganization(
        name: String = "Организация ${UUID.randomUUID()}",
        inn: String = randomInn10(),
    ): Organization =
        organizationRepository.save(
            Organization(
                name = name,
                inn = inn,
            ),
        )

    @Transactional
    fun insertAssignmentGroup(
        name: String = "Группа ${UUID.randomUUID()}",
        description: String? = null,
        operatorIds: MutableSet<UUID> = mutableSetOf(),
    ): AssignmentGroup =
        assignmentGroupRepository.save(
            AssignmentGroup(
                name = name,
                description = description,
                operatorIds = operatorIds,
            ),
        )

    @Transactional
    fun insertSkillGroup(
        name: String = "Скилл-группа ${UUID.randomUUID()}",
        description: String? = null,
        skills: MutableSet<String> = mutableSetOf(),
        operatorIds: MutableSet<UUID> = mutableSetOf(),
    ): SkillGroup =
        skillGroupRepository.save(
            SkillGroup(
                name = name,
                description = description,
                skills = skills,
                operatorIds = operatorIds,
            ),
        )

    @Transactional
    fun insertAppeal(
        subject: String = "Тема",
        channel: AppealChannel = AppealChannel.EMAIL,
        direction: AppealDirection = AppealDirection.INBOUND,
        status: AppealStatus = AppealStatus.PENDING_PROCESSING,
        priority: AppealPriority = AppealPriority.MEDIUM,
        createdById: UUID,
        organizationId: UUID? = null,
        assignmentGroupId: UUID? = null,
        skillGroupId: UUID? = null,
        assignedOperatorId: UUID? = null,
        emailConversationId: UUID? = null,
    ): Appeal =
        appealRepository.save(
            Appeal(
                subject = subject,
                channel = channel,
                direction = direction,
                status = status,
                priority = priority,
                organizationId = organizationId,
                assignmentGroupId = assignmentGroupId,
                skillGroupId = skillGroupId,
                assignedOperatorId = assignedOperatorId,
                emailConversationId = emailConversationId,
                createdById = createdById,
            ),
        )

    companion object {
        fun randomInn10(): String = Random.nextLong(1_000_000_000L, 9_999_999_999L).toString()
    }
}
