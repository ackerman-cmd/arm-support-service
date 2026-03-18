package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.group.AssignmentGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface AssignmentGroupRepository : JpaRepository<AssignmentGroup, UUID> {
    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(
        name: String,
        id: UUID,
    ): Boolean

    @Query("SELECT g FROM AssignmentGroup g WHERE :operatorId MEMBER OF g.operatorIds")
    fun findAllByOperatorId(operatorId: UUID): List<AssignmentGroup>
}
