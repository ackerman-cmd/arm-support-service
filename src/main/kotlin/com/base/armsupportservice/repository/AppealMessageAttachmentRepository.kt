package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.appeal.AppealMessageAttachment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppealMessageAttachmentRepository : JpaRepository<AppealMessageAttachment, UUID> {
    fun findAllByMessageId(messageId: UUID): List<AppealMessageAttachment>

    fun findAllByMessageIdIn(messageIds: Collection<UUID>): List<AppealMessageAttachment>
}
