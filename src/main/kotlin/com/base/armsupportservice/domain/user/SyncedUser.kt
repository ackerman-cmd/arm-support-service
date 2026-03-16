package com.base.armsupportservice.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "synced_users", schema = "arm_support")
class SyncedUser(
    @Id
    val id: UUID,
    @Column(nullable = false)
    var email: String,
    @Column(nullable = false)
    var username: String,
    @Column(name = "first_name")
    var firstName: String? = null,
    @Column(name = "last_name")
    var lastName: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus,
    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "roles", nullable = false, columnDefinition = "TEXT[]")
    var roles: List<String> = emptyList(),
    @Column(name = "synced_at", nullable = false)
    var syncedAt: LocalDateTime,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime,
)
