package com.base.armsupportservice.listener

import com.base.armsupportservice.integration.llm.dto.LlmAppealEnrichedEvent
import com.base.armsupportservice.service.LlmEnrichmentService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Потребляет llm.appeal.enriched — события от llm-service с результатами
 * автоматической классификации обращений: приоритет, тема, краткое саммари.
 * Использует ту же emailKafkaListenerContainerFactory (без type-headers).
 */
@Component
class LlmKafkaListener(
    private val objectMapper: ObjectMapper,
    private val llmEnrichmentService: LlmEnrichmentService,
) {
    private val log = LoggerFactory.getLogger(LlmKafkaListener::class.java)

    @KafkaListener(
        topics = ["\${app.kafka.topics.llm-appeal-enriched}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "emailKafkaListenerContainerFactory",
    )
    fun onAppealEnriched(payload: String) {
        try {
            val event = objectMapper.readValue(payload, LlmAppealEnrichedEvent::class.java)
            log.info(
                "Received llm.appeal.enriched for conversationId={} priority={} topicCode={}",
                event.conversationId,
                event.priority,
                event.topicCode,
            )
            llmEnrichmentService.applyEnrichment(event)
        } catch (ex: Exception) {
            log.error("Failed to process llm.appeal.enriched: payload={}", payload, ex)
            throw ex
        }
    }
}
