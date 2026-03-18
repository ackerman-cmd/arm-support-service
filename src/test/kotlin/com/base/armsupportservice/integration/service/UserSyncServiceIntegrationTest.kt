package com.base.armsupportservice.integration.service

import com.base.armsupportservice.TestDataInsertionUtils
import com.base.armsupportservice.TestDbCleaner
import com.base.armsupportservice.domain.user.UserStatus
import com.base.armsupportservice.event.UserSyncEvent
import com.base.armsupportservice.integration.AbstractIntegrationTest
import com.base.armsupportservice.repository.SyncedUserRepository
import com.base.armsupportservice.service.UserSyncService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserSyncServiceIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var userSyncService: UserSyncService

    @Autowired
    private lateinit var syncedUserRepository: SyncedUserRepository

    @Autowired
    private lateinit var dbCleaner: TestDbCleaner

    @Autowired
    private lateinit var inserter: TestDataInsertionUtils

    @BeforeEach
    fun setUp() {
        dbCleaner.clearAllTables()
    }

    @Test
    fun `handleSync creates new user in database`() {
        val userId = UUID.randomUUID()
        val event = createEvent(userId)

        userSyncService.handleSync(event)

        val saved = syncedUserRepository.findById(userId)
        assertTrue(saved.isPresent)

        val user = saved.get()
        assertEquals(event.email, user.email)
        assertEquals(event.username, user.username)
        assertEquals(event.firstName, user.firstName)
        assertEquals(event.lastName, user.lastName)
        assertEquals(event.status, user.status)
        assertEquals(event.emailVerified, user.emailVerified)
        assertEquals(event.roles, user.roles)
        assertNotNull(user.syncedAt)
        assertNotNull(user.createdAt)
    }

    @Test
    fun `handleSync updates existing user in database`() {
        val userId = UUID.randomUUID()
        inserter.insertSyncedUser(
            id = userId,
            email = "old@example.com",
            username = "olduser",
            status = UserStatus.PENDING_VERIFICATION,
            emailVerified = false,
        )

        val event =
            createEvent(
                userId = userId,
                email = "new@example.com",
                username = "newuser",
                firstName = "New",
                lastName = "User",
                status = UserStatus.ACTIVE,
                emailVerified = true,
                roles = listOf("ROLE_USER", "ROLE_ADMIN"),
                eventType = "USER_PROFILE_UPDATED",
            )

        userSyncService.handleSync(event)

        val user = syncedUserRepository.findById(userId).orElseThrow()
        assertEquals("new@example.com", user.email)
        assertEquals("newuser", user.username)
        assertEquals("New", user.firstName)
        assertEquals("User", user.lastName)
        assertEquals(UserStatus.ACTIVE, user.status)
        assertTrue(user.emailVerified)
        assertEquals(listOf("ROLE_USER", "ROLE_ADMIN"), user.roles)
    }

    @Test
    fun `handleSync preserves createdAt on update`() {
        val userId = UUID.randomUUID()
        val existing = inserter.insertSyncedUser(id = userId)
        val originalCreatedAt = existing.createdAt

        val event = createEvent(userId = userId, eventType = "USER_STATUS_CHANGED")

        userSyncService.handleSync(event)

        val updated = syncedUserRepository.findById(userId).orElseThrow()
        assertEquals(
            originalCreatedAt.truncatedTo(ChronoUnit.MILLIS),
            updated.createdAt.truncatedTo(ChronoUnit.MILLIS),
        )
    }

    @Test
    fun `handleSync processes multiple events for same user`() {
        val userId = UUID.randomUUID()

        userSyncService.handleSync(
            createEvent(userId = userId, email = "first@example.com", eventType = "USER_REGISTERED"),
        )
        userSyncService.handleSync(
            createEvent(userId = userId, email = "second@example.com", eventType = "USER_PROFILE_UPDATED"),
        )

        val user = syncedUserRepository.findById(userId).orElseThrow()
        assertEquals("second@example.com", user.email)
        assertEquals(1, syncedUserRepository.count())
    }

    private fun createEvent(
        userId: UUID = UUID.randomUUID(),
        email: String = "test@example.com",
        username: String = "testuser",
        firstName: String? = "John",
        lastName: String? = "Doe",
        status: UserStatus = UserStatus.ACTIVE,
        emailVerified: Boolean = true,
        roles: List<String> = listOf("ROLE_USER"),
        eventType: String = "USER_REGISTERED",
    ) = UserSyncEvent(
        userId = userId,
        email = email,
        username = username,
        firstName = firstName,
        lastName = lastName,
        status = status,
        emailVerified = emailVerified,
        roles = roles,
        eventType = eventType,
        timestamp = LocalDateTime.now(),
    )
}
