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
 * Скилл-группа — объединяет операторов по набору компетенций (навыков).
 * Позволяет маршрутизировать обращение к специалистам нужного профиля.
 */
@Entity
@Table(name = "skill_groups", schema = "arm_support")
class SkillGroup(
    @Id
    val id: UUID = UUID.randomUUID(),
    var name: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "skill_group_skills",
        schema = "arm_support",
        joinColumns = [JoinColumn(name = "group_id")],
    )
    @Column(name = "skill")
    var skills: MutableSet<String> = mutableSetOf(),
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "skill_group_operators",
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
