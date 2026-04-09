package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.group.SkillGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface SkillGroupRepository : JpaRepository<SkillGroup, UUID> {
    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(
        name: String,
        id: UUID,
    ): Boolean

    @Query("SELECT g FROM SkillGroup g JOIN g.operatorIds oid WHERE oid = :operatorId")
    fun findAllByOperatorId(operatorId: UUID): List<SkillGroup>

    fun findByMailboxEmail(mailboxEmail: String): SkillGroup?
}
