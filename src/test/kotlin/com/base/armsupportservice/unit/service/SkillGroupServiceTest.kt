package com.base.armsupportservice.unit.service

import com.base.armsupportservice.domain.group.SkillGroup
import com.base.armsupportservice.dto.group.SkillGroupRequest
import com.base.armsupportservice.exception.DuplicateResourceException
import com.base.armsupportservice.exception.GroupNotFoundException
import com.base.armsupportservice.exception.OperatorNotFoundException
import com.base.armsupportservice.repository.AssignmentGroupRepository
import com.base.armsupportservice.repository.SkillGroupRepository
import com.base.armsupportservice.repository.SyncedUserRepository
import com.base.armsupportservice.service.GroupMailboxValidator
import com.base.armsupportservice.service.SkillGroupService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SkillGroupServiceTest {
    private val assignmentGroupRepository: AssignmentGroupRepository = mockk(relaxed = true)
    private val skillGroupRepository: SkillGroupRepository = mockk(relaxed = true)
    private val syncedUserRepository: SyncedUserRepository = mockk(relaxed = true)
    private val service =
        SkillGroupService(
            skillGroupRepository,
            syncedUserRepository,
            GroupMailboxValidator(assignmentGroupRepository, skillGroupRepository),
        )

    @Test
    fun `create throws when name duplicate`() {
        val request = SkillGroupRequest(name = "Скиллы", skills = setOf("A"))
        every { skillGroupRepository.existsByName("Скиллы") } returns true

        assertFailsWith<DuplicateResourceException> { service.create(request) }
    }

    @Test
    fun `create throws when operator missing`() {
        val opId = UUID.randomUUID()
        val request = SkillGroupRequest(name = "Скиллы", operatorIds = setOf(opId))
        every { skillGroupRepository.existsByName("Скиллы") } returns false
        every { syncedUserRepository.existsById(opId) } returns false

        assertFailsWith<OperatorNotFoundException> { service.create(request) }
    }

    @Test
    fun `update throws when group missing`() {
        val id = UUID.randomUUID()
        every { skillGroupRepository.findById(id) } returns Optional.empty()

        assertFailsWith<GroupNotFoundException> {
            service.update(id, SkillGroupRequest(name = "X"))
        }
    }

    @Test
    fun `addOperators throws when group missing`() {
        val id = UUID.randomUUID()
        every { skillGroupRepository.findById(id) } returns Optional.empty()

        assertFailsWith<GroupNotFoundException> {
            service.addOperators(id, setOf(UUID.randomUUID()))
        }
    }

    @Test
    fun `delete throws when group missing`() {
        val id = UUID.randomUUID()
        every { skillGroupRepository.existsById(id) } returns false

        assertFailsWith<GroupNotFoundException> { service.delete(id) }
    }

    @Test
    fun `getById returns when present`() {
        val id = UUID.randomUUID()
        val group =
            SkillGroup(
                id = id,
                name = "G",
                skills = mutableSetOf("K1"),
                operatorIds = mutableSetOf(),
            )
        every { skillGroupRepository.findById(id) } returns Optional.of(group)
        every { syncedUserRepository.findAllById(emptySet()) } returns emptyList()

        val response = service.getById(id)
        assertEquals("G", response.name)
        assertTrue(response.skills.contains("K1"))
    }
}
