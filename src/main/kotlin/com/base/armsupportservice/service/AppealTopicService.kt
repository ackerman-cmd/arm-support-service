package com.base.armsupportservice.service

import com.base.armsupportservice.domain.topic.AppealTopic
import com.base.armsupportservice.domain.topic.AppealTopicCategory
import com.base.armsupportservice.dto.topic.AppealTopicRequest
import com.base.armsupportservice.dto.topic.AppealTopicResponse
import com.base.armsupportservice.dto.topic.AppealTopicSummaryResponse
import com.base.armsupportservice.dto.topic.AppealTopicsByCategoryResponse
import com.base.armsupportservice.exception.DuplicateResourceException
import com.base.armsupportservice.exception.GroupNotFoundException
import com.base.armsupportservice.repository.AppealTopicRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AppealTopicService(
    private val topicRepository: AppealTopicRepository,
) {
    fun getAll(): List<AppealTopicResponse> = topicRepository.findAll().map(AppealTopicResponse::from)

    fun getAllActive(): List<AppealTopicResponse> = topicRepository.findAllByActiveTrue().map(AppealTopicResponse::from)

    fun getById(id: UUID): AppealTopicResponse =
        topicRepository
            .findById(id)
            .map(AppealTopicResponse::from)
            .orElseThrow { GroupNotFoundException(id) }

    /** Все активные тематики, сгруппированные по категории — для frontend-dropdown */
    fun getGroupedByCategory(): List<AppealTopicsByCategoryResponse> {
        val topics = topicRepository.findAllByActiveTrue()
        return AppealTopicCategory.entries.mapNotNull { category ->
            val inCategory = topics.filter { it.category == category }
            if (inCategory.isEmpty()) {
                null
            } else {
                AppealTopicsByCategoryResponse(
                    category = category,
                    categoryLabel = category.label,
                    topics = inCategory.map(AppealTopicSummaryResponse::from),
                )
            }
        }
    }

    @Transactional
    fun create(request: AppealTopicRequest): AppealTopicResponse {
        if (topicRepository.existsByCode(request.code)) {
            throw DuplicateResourceException("Тематика с кодом '${request.code}' уже существует")
        }
        val topic =
            AppealTopic(
                code = request.code,
                name = request.name,
                category = request.category,
                description = request.description,
                active = request.active,
            )
        return AppealTopicResponse.from(topicRepository.save(topic))
    }

    @Transactional
    fun update(
        id: UUID,
        request: AppealTopicRequest,
    ): AppealTopicResponse {
        val topic = topicRepository.findById(id).orElseThrow { GroupNotFoundException(id) }
        if (topicRepository.existsByCodeAndIdNot(request.code, id)) {
            throw DuplicateResourceException("Тематика с кодом '${request.code}' уже существует")
        }
        topic.name = request.name
        topic.category = request.category
        topic.description = request.description
        topic.active = request.active
        return AppealTopicResponse.from(topicRepository.save(topic))
    }

    @Transactional
    fun setActive(
        id: UUID,
        active: Boolean,
    ): AppealTopicResponse {
        val topic = topicRepository.findById(id).orElseThrow { GroupNotFoundException(id) }
        topic.active = active
        return AppealTopicResponse.from(topicRepository.save(topic))
    }
}
