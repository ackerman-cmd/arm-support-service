package com.base.armsupportservice.domain.topic

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "appeal_topics", schema = "arm_support")
class AppealTopic(
    @Id
    val id: UUID = UUID.randomUUID(),
    /** Уникальный системный код, используется как стабильный идентификатор в интеграциях */
    @Column(unique = true, nullable = false, length = 64)
    val code: String,
    var name: String,
    @Enumerated(EnumType.STRING)
    var category: AppealTopicCategory,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    /** Неактивные тематики не отображаются при создании обращения, но сохраняются в старых */
    var active: Boolean = true,
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
