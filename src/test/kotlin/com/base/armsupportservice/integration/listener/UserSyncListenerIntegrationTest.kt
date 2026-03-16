package com.base.armsupportservice.integration.listener

import com.base.armsupportservice.TestDataInsertionUtils
import com.base.armsupportservice.TestDbCleaner
import com.base.armsupportservice.domain.user.UserStatus
import com.base.armsupportservice.integration.AbstractIntegrationTest
import com.base.armsupportservice.repository.SyncedUserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserSyncListenerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var syncedUserRepository: SyncedUserRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var dbCleaner: TestDbCleaner

    @Autowired
    private lateinit var inserter: TestDataInsertionUtils

    @Value("\${app.kafka.topics.user-sync}")
    private lateinit var topic: String

    @BeforeEach
    fun setUp() {
        dbCleaner.clearAllTables()
    }

    @Test
    fun `user sync event creates new synced user in database`() {
        val userId = UUID.randomUUID()
        val payload =
            buildPayload(
                userId = userId,
                email = "new@example.com",
                username = "newuser",
                firstName = "John",
                lastName = "Doe",
                status = "ACTIVE",
                emailVerified = true,
                roles = listOf("ROLE_USER"),
                eventType = "USER_REGISTERED",
            )

        sendMessage(userId.toString(), payload, "USER_REGISTERED")

        awaitUntil { syncedUserRepository.findById(userId).isPresent }

        val user = syncedUserRepository.findById(userId).orElseThrow()
        assertEquals("new@example.com", user.email)
        assertEquals("newuser", user.username)
        assertEquals("John", user.firstName)
        assertEquals("Doe", user.lastName)
        assertEquals(UserStatus.ACTIVE, user.status)
        assertTrue(user.emailVerified)
        assertEquals(listOf("ROLE_USER"), user.roles)
        assertNotNull(user.syncedAt)
        assertNotNull(user.createdAt)
    }

    @Test
    fun `user sync event updates existing synced user`() {
        val userId = UUID.randomUUID()
        inserter.insertSyncedUser(
            id = userId,
            email = "old@example.com",
            username = "olduser",
            firstName = "Old",
            lastName = "Name",
            status = UserStatus.PENDING_VERIFICATION,
            emailVerified = false,
            roles = listOf("ROLE_USER"),
        )

        val payload =
            buildPayload(
                userId = userId,
                email = "updated@example.com",
                username = "updateduser",
                firstName = "Updated",
                lastName = "User",
                status = "ACTIVE",
                emailVerified = true,
                roles = listOf("ROLE_USER", "ROLE_ADMIN"),
                eventType = "USER_PROFILE_UPDATED",
            )

        sendMessage(userId.toString(), payload, "USER_PROFILE_UPDATED")

        awaitUntil {
            syncedUserRepository
                .findById(userId)
                .map { it.email == "updated@example.com" }
                .orElse(false)
        }

        val user = syncedUserRepository.findById(userId).orElseThrow()
        assertEquals("updated@example.com", user.email)
        assertEquals("updateduser", user.username)
        assertEquals("Updated", user.firstName)
        assertEquals("User", user.lastName)
        assertEquals(UserStatus.ACTIVE, user.status)
        assertTrue(user.emailVerified)
        assertEquals(listOf("ROLE_USER", "ROLE_ADMIN"), user.roles)
    }

    @Test
    fun `email verified event updates user status`() {
        val userId = UUID.randomUUID()
        inserter.insertSyncedUser(
            id = userId,
            email = "user@example.com",
            username = "user",
            status = UserStatus.PENDING_VERIFICATION,
            emailVerified = false,
        )

        val payload =
            buildPayload(
                userId = userId,
                email = "user@example.com",
                username = "user",
                status = "ACTIVE",
                emailVerified = true,
                roles = listOf("ROLE_USER"),
                eventType = "USER_EMAIL_VERIFIED",
            )

        sendMessage(userId.toString(), payload, "USER_EMAIL_VERIFIED")

        awaitUntil {
            syncedUserRepository
                .findById(userId)
                .map { it.status == UserStatus.ACTIVE && it.emailVerified }
                .orElse(false)
        }

        val user = syncedUserRepository.findById(userId).orElseThrow()
        assertEquals(UserStatus.ACTIVE, user.status)
        assertTrue(user.emailVerified)
    }

    @Test
    fun `status changed event updates user status to blocked`() {
        val userId = UUID.randomUUID()
        inserter.insertSyncedUser(
            id = userId,
            email = "user@example.com",
            username = "blockeduser",
            status = UserStatus.ACTIVE,
        )

        val payload =
            buildPayload(
                userId = userId,
                email = "user@example.com",
                username = "blockeduser",
                status = "BLOCKED",
                emailVerified = true,
                roles = listOf("ROLE_USER"),
                eventType = "USER_STATUS_CHANGED",
            )

        sendMessage(userId.toString(), payload, "USER_STATUS_CHANGED")

        awaitUntil {
            syncedUserRepository
                .findById(userId)
                .map { it.status == UserStatus.BLOCKED }
                .orElse(false)
        }

        val user = syncedUserRepository.findById(userId).orElseThrow()
        assertEquals(UserStatus.BLOCKED, user.status)
    }

    private fun sendMessage(
        key: String,
        payload: String,
        typeId: String,
    ) {
        val record = ProducerRecord<String, String>(topic, null, key, payload)
        record.headers().add("__TypeId__", typeId.toByteArray())
        kafkaTemplate.send(record).get(10, TimeUnit.SECONDS)
    }

    private fun buildPayload(
        userId: UUID,
        email: String,
        username: String,
        firstName: String? = null,
        lastName: String? = null,
        status: String,
        emailVerified: Boolean,
        roles: List<String>,
        eventType: String,
    ): String =
        objectMapper.writeValueAsString(
            mapOf(
                "userId" to userId.toString(),
                "email" to email,
                "username" to username,
                "firstName" to firstName,
                "lastName" to lastName,
                "status" to status,
                "emailVerified" to emailVerified,
                "roles" to roles,
                "eventType" to eventType,
                "timestamp" to LocalDateTime.now().toString(),
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
