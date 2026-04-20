package com.base.armsupportservice.domain.appeal

import com.base.armsupportservice.domain.group.AssignmentGroup
import com.base.armsupportservice.domain.group.SkillGroup
import com.base.armsupportservice.domain.organization.Organization
import com.base.armsupportservice.domain.topic.AppealTopic
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "appeals", schema = "arm_support")
class Appeal(
    @Id
    val id: UUID = UUID.randomUUID(),
    var subject: String,
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    @Enumerated(EnumType.STRING)
    var channel: AppealChannel,
    @Enumerated(EnumType.STRING)
    val direction: AppealDirection,
    @Enumerated(EnumType.STRING)
    var status: AppealStatus,
    @Enumerated(EnumType.STRING)
    var priority: AppealPriority = AppealPriority.MEDIUM,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    var organization: Organization? = null,
    @Column(name = "organization_id")
    var organizationId: UUID? = null,
    var contactName: String? = null,
    var contactEmail: String? = null,
    @Column(length = 32)
    var contactPhone: String? = null,
    var assignedOperatorId: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_group_id", insertable = false, updatable = false)
    var assignmentGroup: AssignmentGroup? = null,
    @Column(name = "assignment_group_id")
    var assignmentGroupId: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_group_id", insertable = false, updatable = false)
    var skillGroup: SkillGroup? = null,
    @Column(name = "skill_group_id")
    var skillGroupId: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    var topic: AppealTopic? = null,
    @Column(name = "topic_id")
    var topicId: UUID? = null,
    /**
     * Операторы, активно работающие с обращением.
     * При прямом назначении — содержит одного оператора.
     * При назначении на группу — пополняется по мере того, как операторы группы
     * присоединяются к обращению через /operators/join.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "appeal_active_operators",
        schema = "arm_support",
        joinColumns = [JoinColumn(name = "appeal_id")],
    )
    @Column(name = "operator_id")
    val activeOperatorIds: MutableSet<UUID> = mutableSetOf(),
    val createdById: UUID,
    @Column(name = "email_conversation_id")
    var emailConversationId: UUID? = null,
    /** VK peer_id пользователя/чата — заполняется для channel=CHAT, используется для отправки ответов */
    @Column(name = "vk_peer_id")
    var vkPeerId: Long? = null,
    var closedAt: LocalDateTime? = null,
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
