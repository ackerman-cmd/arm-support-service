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
@Table(name = "appeal_messages", schema = "arm_support")
class AppealMessage(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appeal_id", nullable = false)
    val appeal: Appeal,
    val senderId: UUID? = null,
    @Enumerated(EnumType.STRING)
    val senderType: MessageSenderType,
    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String,
    @Enumerated(EnumType.STRING)
    val channel: AppealChannel,
    @Column(length = 512)
    val externalMessageId: String? = null,
    @Column(updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
