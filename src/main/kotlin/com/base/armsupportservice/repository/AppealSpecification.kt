package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealPriority
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.dto.appeal.AppealFilterRequest
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime
import java.util.UUID

object AppealSpecification {
    fun byFilter(filter: AppealFilterRequest): Specification<Appeal> =
        Specification { root, _, cb ->
            val predicates = mutableListOf<jakarta.persistence.criteria.Predicate>()

            filter.status?.let {
                predicates += cb.equal(root.get<AppealStatus>("status"), it)
            }
            filter.channel?.let {
                predicates += cb.equal(root.get<AppealChannel>("channel"), it)
            }
            filter.direction?.let {
                predicates += cb.equal(root.get<AppealDirection>("direction"), it)
            }
            filter.priority?.let {
                predicates += cb.equal(root.get<AppealPriority>("priority"), it)
            }
            filter.organizationId?.let {
                predicates += cb.equal(root.get<UUID>("organizationId"), it)
            }
            filter.assignedOperatorId?.let {
                predicates += cb.equal(root.get<UUID>("assignedOperatorId"), it)
            }
            filter.assignmentGroupId?.let {
                predicates += cb.equal(root.get<UUID>("assignmentGroupId"), it)
            }
            filter.skillGroupId?.let {
                predicates += cb.equal(root.get<UUID>("skillGroupId"), it)
            }
            filter.createdById?.let {
                predicates += cb.equal(root.get<UUID>("createdById"), it)
            }
            filter.subject?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(cb.lower(root.get("subject")), "%${it.lowercase()}%")
            }
            filter.contactEmail?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(cb.lower(root.get("contactEmail")), "%${it.lowercase()}%")
            }
            filter.createdFrom?.let {
                predicates += cb.greaterThanOrEqualTo(root.get<LocalDateTime>("createdAt"), it)
            }
            filter.createdTo?.let {
                predicates += cb.lessThanOrEqualTo(root.get<LocalDateTime>("createdAt"), it)
            }

            cb.and(*predicates.toTypedArray())
        }
}
