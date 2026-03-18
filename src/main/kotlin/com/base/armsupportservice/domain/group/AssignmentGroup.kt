package com.base.armsupportservice.domain.group

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Группа назначения — организационный контейнер для операторов.
 * Обращение можно адресовать конкретной группе, а не одному оператору.
 */
@Entity
@Table(name = "assignment_groups", schema = "arm_support")
class AssignmentGroup(
    @Id
    val id: UUID = UUID.randomUUID(),
    var name: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "assignment_group_operators",
        schema = "arm_support",
        joinColumns = [JoinColumn(name = "group_id")],
    )
    @Column(name = "operator_id")
    var operatorIds: MutableSet<UUID> = mutableSetOf(),
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
