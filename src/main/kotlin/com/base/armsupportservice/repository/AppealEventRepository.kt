package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.appeal.AppealEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppealEventRepository : JpaRepository<AppealEvent, UUID> {
    fun findByAppealIdOrderByCreatedAtAsc(
        appealId: UUID,
        pageable: Pageable,
    ): Page<AppealEvent>
}
