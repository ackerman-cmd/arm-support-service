package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface AppealRepository :
    JpaRepository<Appeal, UUID>,
    JpaSpecificationExecutor<Appeal> {
    fun findByEmailConversationId(emailConversationId: UUID): Appeal?

    fun findTopByVkPeerIdOrderByCreatedAtDesc(vkPeerId: Long): Appeal?

    @Query("SELECT a FROM Appeal a WHERE a.vkPeerId = :vkPeerId AND a.status NOT IN :excludedStatuses ORDER BY a.createdAt DESC LIMIT 1")
    fun findActiveByVkPeerId(
        vkPeerId: Long,
        excludedStatuses: Collection<AppealStatus>,
    ): Appeal?

    fun countByAssignedOperatorIdAndStatusIn(
        operatorId: UUID,
        statuses: Collection<AppealStatus>,
    ): Long

    fun countByAssignmentGroupIdAndStatusIn(
        groupId: UUID,
        statuses: Collection<AppealStatus>,
    ): Long
}
