package com.base.armsupportservice.repository

import com.base.armsupportservice.domain.topic.AppealTopic
import com.base.armsupportservice.domain.topic.AppealTopicCategory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface AppealTopicRepository : JpaRepository<AppealTopic, UUID> {
    fun findAllByActiveTrue(): List<AppealTopic>

    fun findAllByActiveTrueAndCategory(category: AppealTopicCategory): List<AppealTopic>

    fun findByCode(code: String): Optional<AppealTopic>

    fun existsByCode(code: String): Boolean

    fun existsByCodeAndIdNot(
        code: String,
        id: UUID,
    ): Boolean
}
