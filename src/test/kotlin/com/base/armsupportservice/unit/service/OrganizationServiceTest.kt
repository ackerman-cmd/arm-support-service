package com.base.armsupportservice.unit.service

import com.base.armsupportservice.domain.organization.Organization
import com.base.armsupportservice.dto.organization.OrganizationRequest
import com.base.armsupportservice.exception.DuplicateResourceException
import com.base.armsupportservice.exception.OrganizationNotFoundException
import com.base.armsupportservice.repository.OrganizationRepository
import com.base.armsupportservice.service.OrganizationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrganizationServiceTest {
    private val organizationRepository: OrganizationRepository = mockk(relaxed = true)
    private val service = OrganizationService(organizationRepository)

    @Test
    fun `create saves when INN is unique`() {
        val request =
            OrganizationRequest(
                name = "ООО Тест",
                inn = "7707083893",
            )
        every { organizationRepository.existsByInn(request.inn) } returns false
        every { organizationRepository.save(any()) } answers { firstArg() }

        val response = service.create(request)

        assertEquals("ООО Тест", response.name)
        assertEquals("7707083893", response.inn)
        verify { organizationRepository.save(any()) }
    }

    @Test
    fun `create throws when INN exists`() {
        val request = OrganizationRequest(name = "X", inn = "7707083893")
        every { organizationRepository.existsByInn(request.inn) } returns true

        assertFailsWith<DuplicateResourceException> { service.create(request) }
    }

    @Test
    fun `getById returns organization`() {
        val id = UUID.randomUUID()
        val org =
            Organization(
                id = id,
                name = "A",
                inn = "7707083894",
            )
        every { organizationRepository.findById(id) } returns Optional.of(org)

        val response = service.getById(id)
        assertEquals("A", response.name)
    }

    @Test
    fun `getById throws when missing`() {
        val id = UUID.randomUUID()
        every { organizationRepository.findById(id) } returns Optional.empty()

        assertFailsWith<OrganizationNotFoundException> { service.getById(id) }
    }

    @Test
    fun `update throws when duplicate INN on other org`() {
        val id = UUID.randomUUID()
        val existing = Organization(id = id, name = "A", inn = "1111111111")
        val request = OrganizationRequest(name = "B", inn = "2222222222")
        every { organizationRepository.findById(id) } returns Optional.of(existing)
        every { organizationRepository.existsByInnAndIdNot(request.inn, id) } returns true

        assertFailsWith<DuplicateResourceException> { service.update(id, request) }
    }

    @Test
    fun `search delegates to repository`() {
        val pageable = PageRequest.of(0, 20)
        every {
            organizationRepository.findByNameContainingIgnoreCase("тест", pageable)
        } returns PageImpl(emptyList())

        service.search("тест", pageable)

        verify { organizationRepository.findByNameContainingIgnoreCase("тест", pageable) }
    }
}
