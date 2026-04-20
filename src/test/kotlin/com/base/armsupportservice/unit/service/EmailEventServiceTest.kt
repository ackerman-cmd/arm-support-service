package com.base.armsupportservice.unit.service

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealMessage
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.domain.appeal.MessageSenderType
import com.base.armsupportservice.integration.email.dto.kafka.EmailConversationCreatedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailConversationMatchedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailInboundPersistedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailOutboundFailedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailOutboundRequestedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailOutboundSentEvent
import com.base.armsupportservice.repository.AppealMessageRepository
import com.base.armsupportservice.repository.AppealRepository
import com.base.armsupportservice.service.EmailEventService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EmailEventServiceTest {
    private val appealRepository: AppealRepository = mockk(relaxed = true)
    private val appealMessageRepository: AppealMessageRepository = mockk(relaxed = true)

    private val service = EmailEventService(appealRepository, appealMessageRepository)

    // ─── helpers ──────────────────────────────────────────────────────────────

    private fun inboundEvent(
        messageId: String = UUID.randomUUID().toString(),
        conversationId: String = UUID.randomUUID().toString(),
        isNewConversation: Boolean = true,
        fromEmail: String = "client@example.com",
        subject: String? = "Test subject",
    ) = EmailInboundPersistedEvent(
        messageId = messageId,
        conversationId = conversationId,
        mailboxId = UUID.randomUUID().toString(),
        caseId = null,
        fromEmail = fromEmail,
        subject = subject,
        internetMessageId = null,
        isNewConversation = isNewConversation,
        receivedAt = Instant.now().toString(),
        textBody = "text",
        htmlBody = "text",
    )

    private fun savedAppeal(
        id: UUID = UUID.randomUUID(),
        status: AppealStatus = AppealStatus.PENDING_PROCESSING,
        emailConversationId: UUID? = null,
    ) = Appeal(
        id = id,
        subject = "Existing",
        channel = AppealChannel.EMAIL,
        direction = AppealDirection.INBOUND,
        status = status,
        emailConversationId = emailConversationId,
        createdById = EmailEventService.SYSTEM_USER_ID,
    )

    // ─── handleInboundPersisted ───────────────────────────────────────────────

    @Test
    fun `new conversation creates appeal with correct fields`() {
        val convId = UUID.randomUUID()
        val event = inboundEvent(conversationId = convId.toString(), isNewConversation = true)

        every { appealMessageRepository.existsByExternalMessageId(event.messageId) } returns false
        every { appealRepository.findByEmailConversationId(convId) } returns null
        every { appealRepository.save(any()) } answers { firstArg() }
        every { appealMessageRepository.save(any()) } answers { firstArg() }

        service.handleInboundPersisted(event)

        val appealSlot = slot<Appeal>()
        verify { appealRepository.save(capture(appealSlot)) }
        with(appealSlot.captured) {
            assertEquals(AppealChannel.EMAIL, channel)
            assertEquals(AppealDirection.INBOUND, direction)
            assertEquals(AppealStatus.PENDING_PROCESSING, status)
            assertEquals(convId, emailConversationId)
            assertEquals("client@example.com", contactEmail)
            assertEquals("Test subject", subject)
            assertEquals(EmailEventService.SYSTEM_USER_ID, createdById)
        }
    }

    @Test
    fun `new conversation creates client message with externalMessageId`() {
        val convId = UUID.randomUUID()
        val msgId = UUID.randomUUID().toString()
        val event = inboundEvent(messageId = msgId, conversationId = convId.toString(), isNewConversation = true)

        every { appealMessageRepository.existsByExternalMessageId(msgId) } returns false
        every { appealRepository.findByEmailConversationId(convId) } returns null
        every { appealRepository.save(any()) } answers { firstArg() }
        every { appealMessageRepository.save(any()) } answers { firstArg() }

        service.handleInboundPersisted(event)

        val msgSlot = slot<AppealMessage>()
        verify { appealMessageRepository.save(capture(msgSlot)) }
        assertEquals(msgId, msgSlot.captured.externalMessageId)
        assertEquals(MessageSenderType.CLIENT, msgSlot.captured.senderType)
        assertEquals(AppealChannel.EMAIL, msgSlot.captured.channel)
    }

    @Test
    fun `duplicate messageId is skipped entirely`() {
        val event = inboundEvent()
        every { appealMessageRepository.existsByExternalMessageId(event.messageId) } returns true

        service.handleInboundPersisted(event)

        verify(exactly = 0) { appealRepository.save(any()) }
        verify(exactly = 0) { appealMessageRepository.save(any()) }
    }

    @Test
    fun `new conversation when appeal already exists adds message only`() {
        val convId = UUID.randomUUID()
        val existing = savedAppeal(emailConversationId = convId)
        val event = inboundEvent(conversationId = convId.toString(), isNewConversation = true)

        every { appealMessageRepository.existsByExternalMessageId(event.messageId) } returns false
        every { appealRepository.findByEmailConversationId(convId) } returns existing
        every { appealMessageRepository.save(any()) } answers { firstArg() }

        service.handleInboundPersisted(event)

        verify(exactly = 0) { appealRepository.save(any()) }
        verify(exactly = 1) { appealMessageRepository.save(any()) }
    }

    @Test
    fun `existing conversation adds message and updates status to IN_PROGRESS`() {
        val convId = UUID.randomUUID()
        val appeal = savedAppeal(emailConversationId = convId, status = AppealStatus.WAITING_CLIENT_RESPONSE)
        val event = inboundEvent(conversationId = convId.toString(), isNewConversation = false)

        every { appealMessageRepository.existsByExternalMessageId(event.messageId) } returns false
        every { appealRepository.findByEmailConversationId(convId) } returns appeal
        every { appealRepository.save(any()) } answers { firstArg() }
        every { appealMessageRepository.save(any()) } answers { firstArg() }

        service.handleInboundPersisted(event)

        assertEquals(AppealStatus.IN_PROGRESS, appeal.status)
        verify { appealRepository.save(appeal) }
        verify { appealMessageRepository.save(any()) }
    }

    @Test
    fun `existing conversation with closed appeal does not change status`() {
        val convId = UUID.randomUUID()
        val appeal = savedAppeal(emailConversationId = convId, status = AppealStatus.CLOSED)
        val event = inboundEvent(conversationId = convId.toString(), isNewConversation = false)

        every { appealMessageRepository.existsByExternalMessageId(event.messageId) } returns false
        every { appealRepository.findByEmailConversationId(convId) } returns appeal
        every { appealMessageRepository.save(any()) } answers { firstArg() }

        service.handleInboundPersisted(event)

        assertEquals(AppealStatus.CLOSED, appeal.status)
        verify(exactly = 0) { appealRepository.save(any()) }
        verify(exactly = 1) { appealMessageRepository.save(any()) }
    }

    @Test
    fun `existing conversation with unknown conversationId logs and skips`() {
        val convId = UUID.randomUUID()
        val event = inboundEvent(conversationId = convId.toString(), isNewConversation = false)

        every { appealMessageRepository.existsByExternalMessageId(event.messageId) } returns false
        every { appealRepository.findByEmailConversationId(convId) } returns null

        service.handleInboundPersisted(event)

        verify(exactly = 0) { appealRepository.save(any()) }
        verify(exactly = 0) { appealMessageRepository.save(any()) }
    }

    @Test
    fun `null subject falls back to placeholder`() {
        val convId = UUID.randomUUID()
        val event = inboundEvent(conversationId = convId.toString(), isNewConversation = true, subject = null)

        every { appealMessageRepository.existsByExternalMessageId(event.messageId) } returns false
        every { appealRepository.findByEmailConversationId(convId) } returns null
        every { appealRepository.save(any()) } answers { firstArg() }
        every { appealMessageRepository.save(any()) } answers { firstArg() }

        service.handleInboundPersisted(event)

        val appealSlot = slot<Appeal>()
        verify { appealRepository.save(capture(appealSlot)) }
        assertEquals("(без темы)", appealSlot.captured.subject)

        val msgSlot = slot<AppealMessage>()
        verify { appealMessageRepository.save(capture(msgSlot)) }
        assertEquals("(без темы)", msgSlot.captured.content)
    }

    @Test
    fun `invalid conversationId UUID logs and skips`() {
        val event = inboundEvent(conversationId = "not-a-uuid", isNewConversation = true)
        every { appealMessageRepository.existsByExternalMessageId(event.messageId) } returns false

        service.handleInboundPersisted(event)

        verify(exactly = 0) { appealRepository.save(any()) }
        verify(exactly = 0) { appealMessageRepository.save(any()) }
    }

    // ─── handleConversationCreated ────────────────────────────────────────────

    @Test
    fun `handleConversationCreated does not touch DB`() {
        val event =
            EmailConversationCreatedEvent(
                conversationId = UUID.randomUUID().toString(),
                mailboxId = UUID.randomUUID().toString(),
                firstMessageId = UUID.randomUUID().toString(),
                fromEmail = "a@b.com",
                subjectNormalized = "Hello",
                createdAt = Instant.now().toString(),
            )
        service.handleConversationCreated(event)

        verify(exactly = 0) { appealRepository.save(any()) }
        verify(exactly = 0) { appealMessageRepository.save(any()) }
    }

    // ─── handleConversationMatched ────────────────────────────────────────────

    @Test
    fun `handleConversationMatched logs warning when no appeal found`() {
        val convId = UUID.randomUUID()
        val event =
            EmailConversationMatchedEvent(
                messageId = UUID.randomUUID().toString(),
                conversationId = convId.toString(),
                caseId = UUID.randomUUID().toString(),
                matchedBy = "SUBJECT",
                matchedAt = Instant.now().toString(),
            )
        every { appealRepository.findByEmailConversationId(convId) } returns null

        service.handleConversationMatched(event)

        verify(exactly = 0) { appealRepository.save(any()) }
    }

    // ─── outbound events ──────────────────────────────────────────────────────

    @Test
    fun `handleOutboundRequested does not touch DB`() {
        val event =
            EmailOutboundRequestedEvent(
                messageId = UUID.randomUUID().toString(),
                conversationId = UUID.randomUUID().toString(),
                createdByUserId = null,
                toEmails = listOf("x@y.com"),
                subject = "S",
                requestedAt = Instant.now().toString(),
            )
        service.handleOutboundRequested(event)

        verify(exactly = 0) { appealRepository.save(any()) }
        verify(exactly = 0) { appealMessageRepository.save(any()) }
    }

    @Test
    fun `handleOutboundSent does not touch DB`() {
        val event =
            EmailOutboundSentEvent(
                messageId = UUID.randomUUID().toString(),
                conversationId = UUID.randomUUID().toString(),
                caseId = null,
                providerMessageId = "resend-123",
                toEmails = listOf("x@y.com"),
                subject = "S",
                sentAt = Instant.now().toString(),
            )
        service.handleOutboundSent(event)

        verify(exactly = 0) { appealRepository.save(any()) }
    }

    @Test
    fun `handleOutboundFailed does not touch DB`() {
        val event =
            EmailOutboundFailedEvent(
                messageId = UUID.randomUUID().toString(),
                conversationId = UUID.randomUUID().toString(),
                caseId = null,
                reason = "BOUNCE",
                failedAt = Instant.now().toString(),
            )
        service.handleOutboundFailed(event)

        verify(exactly = 0) { appealRepository.save(any()) }
    }

    // ─── createdByUserId field ────────────────────────────────────────────────

    @Test
    fun `new appeal uses SYSTEM_USER_ID as createdById`() {
        val convId = UUID.randomUUID()
        val event = inboundEvent(conversationId = convId.toString(), isNewConversation = true)

        every { appealMessageRepository.existsByExternalMessageId(event.messageId) } returns false
        every { appealRepository.findByEmailConversationId(convId) } returns null
        every { appealRepository.save(any()) } answers { firstArg() }
        every { appealMessageRepository.save(any()) } answers { firstArg() }

        service.handleInboundPersisted(event)

        val slot = slot<Appeal>()
        verify { appealRepository.save(capture(slot)) }
        assertEquals(EmailEventService.SYSTEM_USER_ID, slot.captured.createdById)
        assertNull(slot.captured.contactPhone)
    }
}
