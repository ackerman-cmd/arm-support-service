package com.base.armsupportservice.dto.appeal

import com.base.armsupportservice.domain.appeal.AppealEvent
import com.base.armsupportservice.domain.appeal.AppealEventType
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.dto.common.OperatorSummaryResponse
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.UUID

data class AppealEventResponse(
    val id: UUID,
    val appealId: UUID,
    val eventType: AppealEventType,
    val eventTypeLabel: String,
    val initiator: OperatorSummaryResponse?,
    val fromStatus: AppealStatus?,
    val fromStatusLabel: String?,
    val toStatus: AppealStatus?,
    val toStatusLabel: String?,
    val comment: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(
            event: AppealEvent,
            initiator: OperatorSummaryResponse?,
            statusLabels: Map<AppealStatus, String>,
        ) = AppealEventResponse(
            id = event.id,
            appealId = event.appeal.id,
            eventType = event.eventType,
            eventTypeLabel = event.eventType.label,
            initiator = initiator,
            fromStatus = event.fromStatus,
            fromStatusLabel = event.fromStatus?.let { statusLabels[it] ?: it.name },
            toStatus = event.toStatus,
            toStatusLabel = event.toStatus?.let { statusLabels[it] ?: it.name },
            comment = event.comment,
            createdAt = event.createdAt,
        )
    }
}
