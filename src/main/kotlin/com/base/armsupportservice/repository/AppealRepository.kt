package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface AppealRepository :
    JpaRepository<Appeal, UUID>,
    JpaSpecificationExecutor<Appeal> {
    fun findByEmailConversationId(emailConversationId: UUID): Appeal?

    fun countByAssignedOperatorIdAndStatusIn(
        operatorId: UUID,
        statuses: Collection<AppealStatus>,
    ): Long

    fun countByAssignmentGroupIdAndStatusIn(
        groupId: UUID,
        statuses: Collection<AppealStatus>,
    ): Long
}
