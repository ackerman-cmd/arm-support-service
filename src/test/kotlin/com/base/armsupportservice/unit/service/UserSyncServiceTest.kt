package com.base.armsupportservice.unit.service

import com.base.armsupportservice.domain.user.SyncedUser
import com.base.armsupportservice.domain.user.UserStatus
import com.base.armsupportservice.event.UserSyncEvent
import com.base.armsupportservice.repository.SyncedUserRepository
import com.base.armsupportservice.service.UserSyncService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserSyncServiceTest {
    private val syncedUserRepository: SyncedUserRepository = mockk(relaxed = true)
    private val service = UserSyncService(syncedUserRepository)

    @Test
    fun `handleSync creates new user when not exists`() {
        val userId = UUID.randomUUID()
        val event = createEvent(userId)

        every { syncedUserRepository.findById(userId) } returns Optional.empty()
        every { syncedUserRepository.save(any<SyncedUser>()) } answers { firstArg() }

        service.handleSync(event)

        val slot = slot<SyncedUser>()
        verify { syncedUserRepository.save(capture(slot)) }

        val saved = slot.captured
        assertEquals(userId, saved.id)
        assertEquals(event.email, saved.email)
        assertEquals(event.username, saved.username)
        assertEquals(event.firstName, saved.firstName)
        assertEquals(event.lastName, saved.lastName)
        assertEquals(event.status, saved.status)
        assertEquals(event.emailVerified, saved.emailVerified)
        assertEquals(event.roles, saved.roles)
        assertNotNull(saved.syncedAt)
        assertNotNull(saved.createdAt)
    }

    @Test
    fun `handleSync updates existing user`() {
        val userId = UUID.randomUUID()
        val now = LocalDateTime.now()
        val existing =
            SyncedUser(
                id = userId,
                email = "old@example.com",
                username = "olduser",
                firstName = "Old",
                lastName = "Name",
                status = UserStatus.PENDING_VERIFICATION,
                emailVerified = false,
                roles = listOf("ROLE_USER"),
                syncedAt = now.minusDays(1),
                createdAt = now.minusDays(1),
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
            )

        every { syncedUserRepository.findById(userId) } returns Optional.of(existing)
        every { syncedUserRepository.save(any<SyncedUser>()) } answers { firstArg() }

        service.handleSync(event)

        val slot = slot<SyncedUser>()
        verify { syncedUserRepository.save(capture(slot)) }

        val saved = slot.captured
        assertEquals(userId, saved.id)
        assertEquals("new@example.com", saved.email)
        assertEquals("newuser", saved.username)
        assertEquals("New", saved.firstName)
        assertEquals("User", saved.lastName)
        assertEquals(UserStatus.ACTIVE, saved.status)
        assertEquals(true, saved.emailVerified)
        assertEquals(listOf("ROLE_USER", "ROLE_ADMIN"), saved.roles)
    }

    @Test
    fun `handleSync preserves createdAt on update`() {
        val userId = UUID.randomUUID()
        val originalCreatedAt = LocalDateTime.of(2025, 1, 1, 0, 0)
        val existing =
            SyncedUser(
                id = userId,
                email = "user@example.com",
                username = "user",
                status = UserStatus.ACTIVE,
                emailVerified = true,
                roles = listOf("ROLE_USER"),
                syncedAt = originalCreatedAt,
                createdAt = originalCreatedAt,
            )

        every { syncedUserRepository.findById(userId) } returns Optional.of(existing)
        every { syncedUserRepository.save(any<SyncedUser>()) } answers { firstArg() }

        service.handleSync(createEvent(userId))

        val slot = slot<SyncedUser>()
        verify { syncedUserRepository.save(capture(slot)) }
        assertEquals(originalCreatedAt, slot.captured.createdAt)
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
