package com.base.armsupportservice.unit.service

import com.base.armsupportservice.domain.group.AssignmentGroup
import com.base.armsupportservice.domain.group.SkillGroup
import com.base.armsupportservice.domain.user.SyncedUser
import com.base.armsupportservice.domain.user.UserStatus
import com.base.armsupportservice.exception.GroupNotFoundException
import com.base.armsupportservice.exception.MailboxNotConfiguredException
import com.base.armsupportservice.repository.AssignmentGroupRepository
import com.base.armsupportservice.repository.SkillGroupRepository
import com.base.armsupportservice.repository.SyncedUserRepository
import com.base.armsupportservice.service.ActiveMailboxService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ActiveMailboxServiceTest {
    private val assignmentGroupRepository: AssignmentGroupRepository = mockk(relaxed = true)
    private val skillGroupRepository: SkillGroupRepository = mockk(relaxed = true)
    private val syncedUserRepository: SyncedUserRepository = mockk(relaxed = true)
    private val service = ActiveMailboxService(assignmentGroupRepository, skillGroupRepository, syncedUserRepository)

    // ─── getByUser ────────────────────────────────────────────────────────────

    @Test
    fun `getByUser returns mailboxes from both group types`() {
        val user = syncedUser(username = "op1")

        every { syncedUserRepository.findByUsername("op1") } returns user
        every { assignmentGroupRepository.findAllByOperatorId(user.id) } returns
            listOf(assignmentGroup(name = "Группа А", mailboxEmail = "a@test.ru"))
        every { skillGroupRepository.findAllByOperatorId(user.id) } returns
            listOf(skillGroup(name = "Скилл Б", mailboxEmail = "b@test.ru"))

        val result = service.getByUser("op1")

        assertEquals(2, result.size)
        assertTrue(result.any { it.email == "a@test.ru" && it.groupType == "ASSIGNMENT" })
        assertTrue(result.any { it.email == "b@test.ru" && it.groupType == "SKILL" })
    }

    @Test
    fun `getByUser skips groups without mailbox`() {
        val user = syncedUser(username = "op2")

        every { syncedUserRepository.findByUsername("op2") } returns user
        every { assignmentGroupRepository.findAllByOperatorId(user.id) } returns
            listOf(
                assignmentGroup(name = "Без ящика", mailboxEmail = null),
                assignmentGroup(name = "С ящиком", mailboxEmail = "x@test.ru"),
            )
        every { skillGroupRepository.findAllByOperatorId(user.id) } returns emptyList()

        val result = service.getByUser("op2")

        assertEquals(1, result.size)
        assertEquals("x@test.ru", result[0].email)
    }

    @Test
    fun `getByUser returns empty when user has no groups`() {
        val user = syncedUser(username = "op3")

        every { syncedUserRepository.findByUsername("op3") } returns user
        every { assignmentGroupRepository.findAllByOperatorId(user.id) } returns emptyList()
        every { skillGroupRepository.findAllByOperatorId(user.id) } returns emptyList()

        assertTrue(service.getByUser("op3").isEmpty())
    }

    @Test
    fun `getByUser returns empty when username not found in synced users`() {
        every { syncedUserRepository.findByUsername("unknown") } returns null

        assertTrue(service.getByUser("unknown").isEmpty())
    }

    @Test
    fun `getByUser result is sorted by groupName`() {
        val user = syncedUser(username = "op4")

        every { syncedUserRepository.findByUsername("op4") } returns user
        every { assignmentGroupRepository.findAllByOperatorId(user.id) } returns
            listOf(
                assignmentGroup(name = "Цех", mailboxEmail = "c@test.ru"),
                assignmentGroup(name = "Арсенал", mailboxEmail = "a@test.ru"),
            )
        every { skillGroupRepository.findAllByOperatorId(user.id) } returns
            listOf(skillGroup(name = "Маяк", mailboxEmail = "m@test.ru"))

        val names = service.getByUser("op4").map { it.groupName }
        assertEquals(listOf("Арсенал", "Маяк", "Цех"), names)
    }

    // ─── getByAssignmentGroup ─────────────────────────────────────────────────

    @Test
    fun `getByAssignmentGroup returns correct mailbox`() {
        val groupId = UUID.randomUUID()
        val group = assignmentGroup(id = groupId, name = "Линия 1", mailboxEmail = "line1@test.ru")
        every { assignmentGroupRepository.findById(groupId) } returns Optional.of(group)

        val result = service.getByAssignmentGroup(groupId)

        assertEquals("line1@test.ru", result.email)
        assertEquals("ASSIGNMENT", result.groupType)
        assertEquals("Линия 1", result.groupName)
        assertEquals(groupId, result.groupId)
    }

    @Test
    fun `getByAssignmentGroup throws GroupNotFoundException when group missing`() {
        val groupId = UUID.randomUUID()
        every { assignmentGroupRepository.findById(groupId) } returns Optional.empty()

        assertFailsWith<GroupNotFoundException> { service.getByAssignmentGroup(groupId) }
    }

    @Test
    fun `getByAssignmentGroup throws MailboxNotConfiguredException when mailbox null`() {
        val groupId = UUID.randomUUID()
        val group = assignmentGroup(id = groupId, name = "Без ящика", mailboxEmail = null)
        every { assignmentGroupRepository.findById(groupId) } returns Optional.of(group)

        assertFailsWith<MailboxNotConfiguredException> { service.getByAssignmentGroup(groupId) }
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private fun syncedUser(
        id: UUID = UUID.randomUUID(),
        username: String = "user",
    ): SyncedUser {
        val now = LocalDateTime.now()
        return SyncedUser(
            id = id,
            email = "$username@test.local",
            username = username,
            status = UserStatus.ACTIVE,
            syncedAt = now,
            createdAt = now,
        )
    }

    private fun assignmentGroup(
        id: UUID = UUID.randomUUID(),
        name: String = "Group",
        mailboxEmail: String? = null,
    ) = AssignmentGroup(id = id, name = name, mailboxEmail = mailboxEmail)

    private fun skillGroup(
        id: UUID = UUID.randomUUID(),
        name: String = "Skill",
        mailboxEmail: String? = null,
    ) = SkillGroup(id = id, name = name, mailboxEmail = mailboxEmail)
}
