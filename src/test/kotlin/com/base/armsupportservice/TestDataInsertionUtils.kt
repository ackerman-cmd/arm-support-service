package com.base.armsupportservice

import com.base.armsupportservice.domain.user.SyncedUser
import com.base.armsupportservice.domain.user.UserStatus
import com.base.armsupportservice.repository.SyncedUserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
class TestDataInsertionUtils(
    private val syncedUserRepository: SyncedUserRepository,
) {
    @Transactional
    fun insertSyncedUser(
        id: UUID = UUID.randomUUID(),
        email: String = "user${UUID.randomUUID()}@example.com",
        username: String = "testuser${UUID.randomUUID()}",
        firstName: String? = "John",
        lastName: String? = "Doe",
        status: UserStatus = UserStatus.ACTIVE,
        emailVerified: Boolean = true,
        roles: List<String> = listOf("ROLE_USER"),
    ): SyncedUser {
        val now = LocalDateTime.now()
        return syncedUserRepository.save(
            SyncedUser(
                id = id,
                email = email,
                username = username,
                firstName = firstName,
                lastName = lastName,
                status = status,
                emailVerified = emailVerified,
                roles = roles,
                syncedAt = now,
                createdAt = now,
            ),
        )
    }
}
