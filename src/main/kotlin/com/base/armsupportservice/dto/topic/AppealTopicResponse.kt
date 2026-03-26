package com.base.armsupportservice.dto.topic

import com.base.armsupportservice.domain.topic.AppealTopic
import com.base.armsupportservice.domain.topic.AppealTopicCategory
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import java.util.UUID

data class AppealTopicResponse(
    val id: UUID,
    val code: String,
    val name: String,
    val category: AppealTopicCategory,
    val categoryLabel: String,
    val description: String?,
    val active: Boolean,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(topic: AppealTopic) =
            AppealTopicResponse(
                id = topic.id,
                code = topic.code,
                name = topic.name,
                category = topic.category,
                categoryLabel = topic.category.label,
                description = topic.description,
                active = topic.active,
                createdAt = topic.createdAt,
            )
    }
}

/** Компактная форма — для вложения в AppealResponse и справочников фронта */
data class AppealTopicSummaryResponse(
    val id: UUID,
    val code: String,
    val name: String,
    val category: AppealTopicCategory,
    val categoryLabel: String,
) {
    companion object {
        fun from(topic: AppealTopic) =
            AppealTopicSummaryResponse(
                id = topic.id,
                code = topic.code,
                name = topic.name,
                category = topic.category,
                categoryLabel = topic.category.label,
            )
    }
}

/** Группировка тематик по категории — для dropdown на фронте */
data class AppealTopicsByCategoryResponse(
    val category: AppealTopicCategory,
    val categoryLabel: String,
    val topics: List<AppealTopicSummaryResponse>,
)
