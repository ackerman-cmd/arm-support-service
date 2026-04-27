package com.base.armsupportservice.service

import com.base.armsupportservice.domain.appeal.AppealPriority
import com.base.armsupportservice.integration.llm.dto.LlmAppealEnrichedEvent
import com.base.armsupportservice.repository.AppealRepository
import com.base.armsupportservice.repository.AppealTopicRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class LlmEnrichmentService(
    private val appealRepository: AppealRepository,
    private val appealTopicRepository: AppealTopicRepository,
) {
    private val log = LoggerFactory.getLogger(LlmEnrichmentService::class.java)

    @Transactional
    fun applyEnrichment(event: LlmAppealEnrichedEvent) {
        val conversationId = UUID.fromString(event.conversationId)
        val appeal = appealRepository.findByEmailConversationId(conversationId)
        if (appeal == null) {
            log.warn(
                "Appeal not found for conversationId={}, enrichment skipped (appeal may not be created yet)",
                event.conversationId,
            )
            return
        }

        // Обновляем приоритет только если LLM предложил более высокий
        val llmPriority = runCatching { AppealPriority.valueOf(event.priority) }.getOrNull()
        if (llmPriority != null && llmPriority.ordinal > appeal.priority.ordinal) {
            log.info(
                "Upgrading appeal={} priority {} → {}",
                appeal.id,
                appeal.priority,
                llmPriority,
            )
            appeal.priority = llmPriority
        }

        // Проставляем топик, если ещё не задан вручную
        if (appeal.topicId == null && event.topicCode != null) {
            appealTopicRepository.findByCode(event.topicCode).ifPresent { topic ->
                appeal.topicId = topic.id
                log.info("Set topic={} for appeal={}", event.topicCode, appeal.id)
            }
        }

        // Саммари всегда перезаписываем (первый раз ставим)
        appeal.summary = event.summary

        appealRepository.save(appeal)
        log.info("LLM enrichment applied for appeal={}", appeal.id)
    }
}
