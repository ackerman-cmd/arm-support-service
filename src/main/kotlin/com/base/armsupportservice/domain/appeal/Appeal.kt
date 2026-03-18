package com.base.armsupportservice.domain.appeal

import com.base.armsupportservice.domain.group.AssignmentGroup
import com.base.armsupportservice.domain.group.SkillGroup
import com.base.armsupportservice.domain.organization.Organization
import jakarta.persistence.Column
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
    /**
     * FK на Organization — используется для установки связи и фильтрации через Specification.
     * Обновляется напрямую, organization (proxy) обновляется через него.
     */
    @Column(name = "organization_id")
    var organizationId: UUID? = null,
    var contactName: String? = null,
    var contactEmail: String? = null,
    @Column(length = 32)
    var contactPhone: String? = null,
    /** UUID оператора из synced_users, которому назначено обращение */
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
    /** Оператор, создавший обращение */
    val createdById: UUID,
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
