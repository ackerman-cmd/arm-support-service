package com.base.armsupportservice.dto.appeal

import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealMessage
import com.base.armsupportservice.domain.appeal.MessageSenderType
import com.base.armsupportservice.dto.common.OperatorSummaryResponse
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.UUID

data class AppealMessageResponse(
    val id: UUID,
    val appealId: UUID,
    val senderType: MessageSenderType,
    val sender: OperatorSummaryResponse?,
    val content: String,
    val channel: AppealChannel,
    val externalMessageId: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(
            message: AppealMessage,
            sender: OperatorSummaryResponse?,
        ) = AppealMessageResponse(
            id = message.id,
            appealId = message.appeal.id,
            senderType = message.senderType,
            sender = sender,
            content = message.content,
            channel = message.channel,
            externalMessageId = message.externalMessageId,
            createdAt = message.createdAt,
        )
    }
}
