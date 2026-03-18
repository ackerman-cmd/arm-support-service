package com.base.armsupportservice.domain.organization

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "organizations", schema = "arm_support")
class Organization(
    @Id
    val id: UUID = UUID.randomUUID(),
    var name: String,
    /** ИНН — уникален */
    @Column(unique = true, nullable = false, length = 12)
    var inn: String,
    /** КПП */
    @Column(length = 9)
    var kpp: String? = null,
    /** ОГРН */
    @Column(length = 15)
    var ogrn: String? = null,
    @Column(columnDefinition = "TEXT")
    var legalAddress: String? = null,
    var contactEmail: String? = null,
    @Column(length = 32)
    var contactPhone: String? = null,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @PrePersist
    fun prePersist() {
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
