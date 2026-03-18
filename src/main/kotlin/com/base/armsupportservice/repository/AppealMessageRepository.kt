package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.appeal.AppealMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppealMessageRepository : JpaRepository<AppealMessage, UUID> {
    fun findByAppealIdOrderByCreatedAtAsc(
        appealId: UUID,
        pageable: Pageable,
    ): Page<AppealMessage>

    fun existsByExternalMessageId(externalMessageId: String): Boolean
}
