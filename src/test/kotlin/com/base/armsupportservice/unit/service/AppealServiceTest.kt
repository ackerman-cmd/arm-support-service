package com.base.armsupportservice.unit.service

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealMessage
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.domain.appeal.MessageSenderType
import com.base.armsupportservice.domain.user.SyncedUser
import com.base.armsupportservice.domain.user.UserStatus
import com.base.armsupportservice.dto.appeal.AppealFilterRequest
import com.base.armsupportservice.dto.appeal.AppealMessageRequest
import com.base.armsupportservice.dto.appeal.AppealRequest
import com.base.armsupportservice.dto.appeal.AssignOperatorRequest
import com.base.armsupportservice.exception.AppealNotFoundException
import com.base.armsupportservice.exception.InvalidStatusTransitionException
import com.base.armsupportservice.exception.OrganizationNotFoundException
import com.base.armsupportservice.repository.AppealEventRepository
import com.base.armsupportservice.repository.AppealMessageRepository
import com.base.armsupportservice.repository.AppealRepository
import com.base.armsupportservice.repository.AppealTopicRepository
import com.base.armsupportservice.repository.AssignmentGroupRepository
import com.base.armsupportservice.repository.OrganizationRepository
import com.base.armsupportservice.repository.SkillGroupRepository
import com.base.armsupportservice.repository.SyncedUserRepository
import com.base.armsupportservice.security.UserPrincipal
import com.base.armsupportservice.service.AppealService
import com.base.armsupportservice.service.PermissionService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppealServiceTest {
    private val appealRepository: AppealRepository = mockk(relaxed = true)
    private val appealMessageRepository: AppealMessageRepository = mockk(relaxed = true)
    private val appealEventRepository: AppealEventRepository = mockk(relaxed = true)
    private val organizationRepository: OrganizationRepository = mockk(relaxed = true)
    private val assignmentGroupRepository: AssignmentGroupRepository = mockk(relaxed = true)
    private val skillGroupRepository: SkillGroupRepository = mockk(relaxed = true)
    private val syncedUserRepository: SyncedUserRepository = mockk(relaxed = true)
    private val appealTopicRepository: AppealTopicRepository = mockk(relaxed = true)
    private val permissionService: PermissionService = PermissionService()

    private val service =
        AppealService(
            appealRepository,
            appealMessageRepository,
            appealEventRepository,
            organizationRepository,
            assignmentGroupRepository,
            skillGroupRepository,
            syncedUserRepository,
            appealTopicRepository,
            permissionService,
        )

    private val userId: UUID = UUID.randomUUID()

    @BeforeEach
    fun stubAppealEventSave() {
        every { appealEventRepository.save(any()) } answers { firstArg() }
    }

    private fun inboundAppeal(
        id: UUID = UUID.randomUUID(),
        status: AppealStatus = AppealStatus.PENDING_PROCESSING,
    ) = Appeal(
        id = id,
        subject = "Subj",
        channel = AppealChannel.EMAIL,
        direction = AppealDirection.INBOUND,
        status = status,
        createdById = userId,
    )

    @Test
    fun `getById throws when missing`() {
        val id = UUID.randomUUID()
        every { appealRepository.findById(id) } returns Optional.empty()
        assertFailsWith<AppealNotFoundException> { service.getById(id) }
    }

    @Test
    fun `create sets createdById and initial status`() {
        val principal =
            UserPrincipal(
                userId,
                "u",
                "e@e.com",
                listOf("USER"),
                listOf("APPEAL_WRITE"),
                "ACTIVE",
            )
        val request =
            AppealRequest(
                subject = "S",
                channel = AppealChannel.CHAT,
                direction = AppealDirection.INBOUND,
            )
        lateinit var savedAppeal: Appeal
        every { appealRepository.save(any()) } answers {
            savedAppeal = firstArg()
            savedAppeal
        }

        val result = service.create(request, principal)

        assertEquals("S", result.subject)
        assertEquals(userId, savedAppeal.createdById)
        assertEquals(AppealStatus.PENDING_PROCESSING, savedAppeal.status)
    }

    @Test
    fun `create throws when organization not found`() {
        val orgId = UUID.randomUUID()
        val principal =
            UserPrincipal(userId, "u", "e@e.com", emptyList(), emptyList(), "ACTIVE")
        val request =
            AppealRequest(
                subject = "S",
                channel = AppealChannel.EMAIL,
                direction = AppealDirection.INBOUND,
                organizationId = orgId,
            )
        every { organizationRepository.existsById(orgId) } returns false
        assertFailsWith<OrganizationNotFoundException> { service.create(request, principal) }
    }

    @Test
    fun `takeIntoWork assigns operator`() {
        val id = UUID.randomUUID()
        val appeal = inboundAppeal(id = id)
        val principal =
            UserPrincipal(userId, "op", "o@o.com", emptyList(), emptyList(), "ACTIVE")
        val syncedOp =
            SyncedUser(
                id = userId,
                email = "o@o.com",
                username = "op",
                status = UserStatus.ACTIVE,
                syncedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
            )
        every { appealRepository.findById(id) } returns Optional.of(appeal)
        every { appealRepository.save(any()) } answers { firstArg() }
        every { syncedUserRepository.findById(userId) } returns Optional.of(syncedOp)

        val result = service.takeIntoWork(id, principal)

        assertEquals(AppealStatus.IN_PROGRESS, result.status)
        assertEquals(userId, result.assignedOperator?.id)
    }

    @Test
    fun `assign throws when operator inactive`() {
        val appeal = inboundAppeal(status = AppealStatus.IN_PROGRESS)
        val opId = UUID.randomUUID()
        val user =
            SyncedUser(
                id = opId,
                email = "x",
                username = "x",
                status = UserStatus.INACTIVE,
                syncedAt = LocalDateTime.now(),
                createdAt = LocalDateTime.now(),
            )
        every { appealRepository.findById(appeal.id) } returns Optional.of(appeal)
        every { syncedUserRepository.findById(opId) } returns Optional.of(user)

        assertFailsWith<IllegalStateException> {
            service.assign(appeal.id, AssignOperatorRequest(operatorId = opId))
        }
    }

    @Test
    fun `receiveClientMessage deduplicates by external id`() {
        val appeal = inboundAppeal(status = AppealStatus.WAITING_CLIENT_RESPONSE)
        val msg =
            AppealMessage(
                appeal = appeal,
                senderType = MessageSenderType.CLIENT,
                content = "hello",
                channel = AppealChannel.EMAIL,
                externalMessageId = "ext-99",
            )
        every { appealRepository.findById(appeal.id) } returns Optional.of(appeal)
        every { appealMessageRepository.existsByExternalMessageId("ext-99") } returns true
        every {
            appealMessageRepository.findByAppealIdOrderByCreatedAtAsc(appeal.id, any<Pageable>())
        } returns PageImpl(listOf(msg))

        val req =
            AppealMessageRequest(
                content = "x",
                channel = AppealChannel.EMAIL,
                externalMessageId = "ext-99",
            )
        val result = service.receiveClientMessage(appeal.id, req)

        assertEquals("hello", result.content)
        verify(exactly = 0) { appealMessageRepository.save(any()) }
    }

    @Test
    fun `changeStatus throws on invalid transition`() {
        val appeal = inboundAppeal(status = AppealStatus.CLOSED)
        every { appealRepository.findById(appeal.id) } returns Optional.of(appeal)
        assertFailsWith<InvalidStatusTransitionException> {
            service.changeStatus(appeal.id, AppealStatus.IN_PROGRESS)
        }
    }

    @Test
    fun `filter returns page from repository`() {
        val appeal = inboundAppeal()
        every {
            appealRepository.findAll(any<Specification<Appeal>>(), any<Pageable>())
        } returns PageImpl(listOf(appeal), PageRequest.of(0, 20), 1)

        val page = service.filter(AppealFilterRequest(page = 0, size = 20))
        assertEquals(1, page.totalElements)
    }

    @Test
    fun `delete removes appeal`() {
        val id = UUID.randomUUID()
        every { appealRepository.existsById(id) } returns true
        every { appealRepository.deleteById(id) } just Runs

        service.delete(id)
        verify { appealRepository.deleteById(id) }
    }
}
