package com.base.armsupportservice.service

import com.base.armsupportservice.domain.user.SyncedUser
import com.base.armsupportservice.event.UserSyncEvent
import com.base.armsupportservice.repository.SyncedUserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserSyncService(
    private val syncedUserRepository: SyncedUserRepository,
) {
    private val log = LoggerFactory.getLogger(UserSyncService::class.java)

    @Transactional
    fun handleSync(event: UserSyncEvent) {
        val existing = syncedUserRepository.findById(event.userId).orElse(null)

        if (existing != null) {
            existing.email = event.email
            existing.username = event.username
            existing.firstName = event.firstName
            existing.lastName = event.lastName
            existing.status = event.status
            existing.emailVerified = event.emailVerified
            existing.roles = event.roles
            existing.syncedAt = LocalDateTime.now()
            syncedUserRepository.save(existing)
            log.info("Updated synced user: userId={}, eventType={}", event.userId, event.eventType)
        } else {
            val now = LocalDateTime.now()
            syncedUserRepository.save(
                SyncedUser(
                    id = event.userId,
                    email = event.email,
                    username = event.username,
                    firstName = event.firstName,
                    lastName = event.lastName,
                    status = event.status,
                    emailVerified = event.emailVerified,
                    roles = event.roles,
                    syncedAt = now,
                    createdAt = now,
                ),
            )
            log.info("Created synced user: userId={}, eventType={}", event.userId, event.eventType)
        }
    }
}
