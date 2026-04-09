package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.user.SyncedUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SyncedUserRepository : JpaRepository<SyncedUser, UUID> {
    fun findByUsername(username: String): SyncedUser?
}
