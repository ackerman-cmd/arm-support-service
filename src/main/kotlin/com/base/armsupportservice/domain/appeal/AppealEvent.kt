package com.base.armsupportservice.domain.appeal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "appeal_events", schema = "arm_support")
class AppealEvent(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appeal_id", nullable = false)
    val appeal: Appeal,
    @Enumerated(EnumType.STRING)
    val eventType: AppealEventType,
    /** Кто инициировал событие; null — системное действие */
    val initiatorId: UUID? = null,
    @Enumerated(EnumType.STRING)
    val fromStatus: AppealStatus? = null,
    @Enumerated(EnumType.STRING)
    val toStatus: AppealStatus? = null,
    /** Дополнительный контекст: имя оператора, название группы и т.п. */
    @Column(length = 512)
    val comment: String? = null,
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
