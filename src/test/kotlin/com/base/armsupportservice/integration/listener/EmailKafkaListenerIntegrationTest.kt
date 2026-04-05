package com.base.armsupportservice.integration.listener

import com.base.armsupportservice.TestDataInsertionUtils
import com.base.armsupportservice.TestDbCleaner
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.integration.AbstractIntegrationTest
import com.base.armsupportservice.repository.AppealMessageRepository
import com.base.armsupportservice.repository.AppealRepository
import com.base.armsupportservice.service.EmailEventService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EmailKafkaListenerIntegrationTest : AbstractIntegrationTest() {
    @Autowired private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired private lateinit var objectMapper: ObjectMapper

    @Autowired private lateinit var appealRepository: AppealRepository

    @Autowired private lateinit var appealMessageRepository: AppealMessageRepository

    @Autowired private lateinit var dbCleaner: TestDbCleaner

    @Autowired private lateinit var inserter: TestDataInsertionUtils

    @Value("\${app.kafka.topics.email-inbound-persisted}")
    private lateinit var inboundPersistedTopic: String

    @Value("\${app.kafka.topics.email-conversation-created}")
    private lateinit var conversationCreatedTopic: String

    @Value("\${app.kafka.topics.email-conversation-matched}")
    private lateinit var conversationMatchedTopic: String

    @Value("\${app.kafka.topics.email-outbound-requested}")
    private lateinit var outboundRequestedTopic: String

    @Value("\${app.kafka.topics.email-outbound-sent}")
    private lateinit var outboundSentTopic: String

    @Value("\${app.kafka.topics.email-outbound-failed}")
    private lateinit var outboundFailedTopic: String

    @BeforeEach
    fun setUp() {
        dbCleaner.clearAllTables()
    }

    // ─── email.inbound.persisted ──────────────────────────────────────────────

    @Test
    fun `inbound persisted with new conversation creates appeal and message`() {
        val convId = UUID.randomUUID()
        val msgId = UUID.randomUUID().toString()

        send(
            inboundPersistedTopic,
            convId.toString(),
            inboundPersistedPayload(
                messageId = msgId,
                conversationId = convId.toString(),
                isNewConversation = true,
                fromEmail = "client@bank.com",
                subject = "Проблема с картой",
            ),
        )

        awaitUntil { appealRepository.findByEmailConversationId(convId) != null }

        val appeal = appealRepository.findByEmailConversationId(convId)!!
        assertEquals(AppealChannel.EMAIL, appeal.channel)
        assertEquals(AppealDirection.INBOUND, appeal.direction)
        assertEquals(AppealStatus.PENDING_PROCESSING, appeal.status)
        assertEquals("Проблема с картой", appeal.subject)
        assertEquals("client@bank.com", appeal.contactEmail)
        assertEquals(EmailEventService.SYSTEM_USER_ID, appeal.createdById)

        val messages = appealMessageRepository.findAll().filter { it.appeal.id == appeal.id }
        assertEquals(1, messages.size)
        assertEquals(msgId, messages[0].externalMessageId)
    }

    @Test
    fun `inbound persisted with existing conversation adds message and updates status`() {
        val convId = UUID.randomUUID()
        val appeal =
            inserter.insertAppeal(
                subject = "Существующее обращение",
                channel = AppealChannel.EMAIL,
                direction = AppealDirection.INBOUND,
                status = AppealStatus.WAITING_CLIENT_RESPONSE,
                createdById = EmailEventService.SYSTEM_USER_ID,
                emailConversationId = convId,
            )

        val msgId = UUID.randomUUID().toString()
        send(
            inboundPersistedTopic,
            convId.toString(),
            inboundPersistedPayload(
                messageId = msgId,
                conversationId = convId.toString(),
                isNewConversation = false,
                fromEmail = "client@bank.com",
                subject = "Ответ клиента",
            ),
        )

        awaitUntil {
            appealMessageRepository.existsByExternalMessageId(msgId)
        }

        val updated = appealRepository.findById(appeal.id).orElseThrow()
        assertEquals(AppealStatus.IN_PROGRESS, updated.status)

        val messages = appealMessageRepository.findAll().filter { it.appeal.id == appeal.id }
        assertEquals(1, messages.size)
        assertEquals(msgId, messages[0].externalMessageId)
    }

    @Test
    fun `duplicate inbound persisted message is ignored`() {
        val convId = UUID.randomUUID()
        val msgId = UUID.randomUUID().toString()

        val payload =
            inboundPersistedPayload(
                messageId = msgId,
                conversationId = convId.toString(),
                isNewConversation = true,
            )

        send(inboundPersistedTopic, convId.toString(), payload)
        awaitUntil { appealRepository.findByEmailConversationId(convId) != null }

        send(inboundPersistedTopic, convId.toString(), payload)
        Thread.sleep(2_000)

        val count = appealRepository.findAll().count { it.emailConversationId == convId }
        assertEquals(1, count)

        val messages = appealMessageRepository.findAll().filter { it.externalMessageId == msgId }
        assertEquals(1, messages.size)
    }

    @Test
    fun `inbound persisted with null subject uses placeholder`() {
        val convId = UUID.randomUUID()
        val msgId = UUID.randomUUID().toString()

        send(
            inboundPersistedTopic,
            convId.toString(),
            inboundPersistedPayload(
                messageId = msgId,
                conversationId = convId.toString(),
                isNewConversation = true,
                subject = null,
            ),
        )

        awaitUntil { appealRepository.findByEmailConversationId(convId) != null }

        val appeal = appealRepository.findByEmailConversationId(convId)!!
        assertEquals("(без темы)", appeal.subject)

        val messages = appealMessageRepository.findAll().filter { it.appeal.id == appeal.id }
        assertEquals("(без темы)", messages[0].content)
    }

    @Test
    fun `inbound persisted for closed appeal adds message but keeps status`() {
        val convId = UUID.randomUUID()
        val appeal =
            inserter.insertAppeal(
                subject = "Закрытое обращение",
                channel = AppealChannel.EMAIL,
                status = AppealStatus.CLOSED,
                createdById = EmailEventService.SYSTEM_USER_ID,
                emailConversationId = convId,
            )

        val msgId = UUID.randomUUID().toString()
        send(
            inboundPersistedTopic,
            convId.toString(),
            inboundPersistedPayload(
                messageId = msgId,
                conversationId = convId.toString(),
                isNewConversation = false,
            ),
        )

        awaitUntil { appealMessageRepository.existsByExternalMessageId(msgId) }

        val updated = appealRepository.findById(appeal.id).orElseThrow()
        assertEquals(AppealStatus.CLOSED, updated.status)
    }

    // ─── email.conversation.created ───────────────────────────────────────────

    @Test
    fun `conversation created event is consumed without error`() {
        val convId = UUID.randomUUID()

        send(
            conversationCreatedTopic,
            convId.toString(),
            objectMapper.writeValueAsString(
                mapOf(
                    "conversation_id" to convId.toString(),
                    "mailbox_id" to UUID.randomUUID().toString(),
                    "first_message_id" to UUID.randomUUID().toString(),
                    "from_email" to "client@example.com",
                    "subject_normalized" to "Hello",
                    "created_at" to Instant.now().toString(),
                ),
            ),
        )

        Thread.sleep(1_500)
        assertEquals(0, appealRepository.count())
    }

    // ─── email.conversation.matched ───────────────────────────────────────────

    @Test
    fun `conversation matched event is consumed without error`() {
        val convId = UUID.randomUUID()

        send(
            conversationMatchedTopic,
            convId.toString(),
            objectMapper.writeValueAsString(
                mapOf(
                    "message_id" to UUID.randomUUID().toString(),
                    "conversation_id" to convId.toString(),
                    "case_id" to null,
                    "matched_by" to "SUBJECT",
                    "matched_at" to Instant.now().toString(),
                ),
            ),
        )

        Thread.sleep(1_500)
        assertNull(appealRepository.findByEmailConversationId(convId))
    }

    // ─── email.outbound.* ─────────────────────────────────────────────────────

    @Test
    fun `outbound requested event is consumed without error`() {
        val msgId = UUID.randomUUID().toString()
        val convId = UUID.randomUUID().toString()

        send(
            outboundRequestedTopic,
            msgId,
            objectMapper.writeValueAsString(
                mapOf(
                    "message_id" to msgId,
                    "conversation_id" to convId,
                    "created_by_user_id" to null,
                    "to_emails" to listOf("a@b.com"),
                    "subject" to "Test",
                    "requested_at" to Instant.now().toString(),
                ),
            ),
        )

        Thread.sleep(1_500)
        assertEquals(0, appealRepository.count())
    }

    @Test
    fun `outbound sent event is consumed without error`() {
        val msgId = UUID.randomUUID().toString()

        send(
            outboundSentTopic,
            msgId,
            objectMapper.writeValueAsString(
                mapOf(
                    "message_id" to msgId,
                    "conversation_id" to UUID.randomUUID().toString(),
                    "case_id" to null,
                    "provider_message_id" to "resend-xyz",
                    "to_emails" to listOf("a@b.com"),
                    "subject" to "Sent",
                    "sent_at" to Instant.now().toString(),
                ),
            ),
        )

        Thread.sleep(1_500)
        assertEquals(0, appealMessageRepository.count())
    }

    @Test
    fun `outbound failed event is consumed without error`() {
        val msgId = UUID.randomUUID().toString()

        send(
            outboundFailedTopic,
            msgId,
            objectMapper.writeValueAsString(
                mapOf(
                    "message_id" to msgId,
                    "conversation_id" to UUID.randomUUID().toString(),
                    "case_id" to null,
                    "reason" to "BOUNCE",
                    "failed_at" to Instant.now().toString(),
                ),
            ),
        )

        Thread.sleep(1_500)
        assertEquals(0, appealMessageRepository.count())
    }

    // ─── malformed payload ────────────────────────────────────────────────────

    @Test
    fun `malformed JSON is skipped after retries without crashing consumer`() {
        val convId = UUID.randomUUID()

        send(inboundPersistedTopic, convId.toString(), "{invalid-json}")

        Thread.sleep(4_000)

        val validMsgId = UUID.randomUUID().toString()
        val validConvId = UUID.randomUUID()
        send(
            inboundPersistedTopic,
            validConvId.toString(),
            inboundPersistedPayload(
                messageId = validMsgId,
                conversationId = validConvId.toString(),
                isNewConversation = true,
            ),
        )

        awaitUntil { appealRepository.findByEmailConversationId(validConvId) != null }
        assertNotNull(appealRepository.findByEmailConversationId(validConvId))
    }

    // ─── private helpers ──────────────────────────────────────────────────────

    private fun send(
        topic: String,
        key: String,
        payload: String,
    ) {
        kafkaTemplate.send(topic, key, payload).get(10, TimeUnit.SECONDS)
    }

    private fun inboundPersistedPayload(
        messageId: String = UUID.randomUUID().toString(),
        conversationId: String = UUID.randomUUID().toString(),
        mailboxId: String = UUID.randomUUID().toString(),
        isNewConversation: Boolean = true,
        fromEmail: String = "client@example.com",
        subject: String? = "Test subject",
    ): String =
        objectMapper.writeValueAsString(
            mapOf(
                "message_id" to messageId,
                "conversation_id" to conversationId,
                "mailbox_id" to mailboxId,
                "case_id" to null,
                "from_email" to fromEmail,
                "subject" to subject,
                "internet_message_id" to null,
                "is_new_conversation" to isNewConversation,
                "received_at" to Instant.now().toString(),
            ),
        )

    private fun awaitUntil(
        timeoutMs: Long = 10_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(200)
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }
}
