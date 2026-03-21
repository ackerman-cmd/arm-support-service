package com.base.armsupportservice.unit.service

import com.base.armsupportservice.domain.group.AssignmentGroup
import com.base.armsupportservice.dto.group.AssignmentGroupRequest
import com.base.armsupportservice.exception.DuplicateResourceException
import com.base.armsupportservice.exception.GroupNotFoundException
import com.base.armsupportservice.exception.OperatorNotFoundException
import com.base.armsupportservice.repository.AssignmentGroupRepository
import com.base.armsupportservice.repository.SyncedUserRepository
import com.base.armsupportservice.service.AssignmentGroupService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AssignmentGroupServiceTest {
    private val assignmentGroupRepository: AssignmentGroupRepository = mockk(relaxed = true)
    private val syncedUserRepository: SyncedUserRepository = mockk(relaxed = true)
    private val service = AssignmentGroupService(assignmentGroupRepository, syncedUserRepository)

    @Test
    fun `create throws when name duplicate`() {
        val request = AssignmentGroupRequest(name = "Группа")
        every { assignmentGroupRepository.existsByName("Группа") } returns true

        assertFailsWith<DuplicateResourceException> { service.create(request) }
    }

    @Test
    fun `create throws when operator missing`() {
        val opId = UUID.randomUUID()
        val request = AssignmentGroupRequest(name = "Группа", operatorIds = setOf(opId))
        every { assignmentGroupRepository.existsByName("Группа") } returns false
        every { syncedUserRepository.existsById(opId) } returns false

        assertFailsWith<OperatorNotFoundException> { service.create(request) }
    }

    @Test
    fun `getById throws when missing`() {
        val id = UUID.randomUUID()
        every { assignmentGroupRepository.findById(id) } returns Optional.empty()

        assertFailsWith<GroupNotFoundException> { service.getById(id) }
    }

    @Test
    fun `delete throws when missing`() {
        val id = UUID.randomUUID()
        every { assignmentGroupRepository.existsById(id) } returns false

        assertFailsWith<GroupNotFoundException> { service.delete(id) }
    }

    @Test
    fun `removeOperator succeeds without existence check on operator`() {
        val id = UUID.randomUUID()
        val opId = UUID.randomUUID()
        val group =
            AssignmentGroup(
                id = id,
                name = "G",
                operatorIds = mutableSetOf(opId),
            )
        every { assignmentGroupRepository.findById(id) } returns Optional.of(group)
        every { assignmentGroupRepository.save(any()) } answers { firstArg() }
        every { syncedUserRepository.findAllById(emptySet()) } returns emptyList()

        val response = service.removeOperator(id, opId)

        assertEquals(0, response.operatorCount)
    }
}
