package com.base.armsupportservice.listener

import com.base.armsupportservice.integration.email.dto.kafka.EmailConversationCreatedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailConversationMatchedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailInboundPersistedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailOutboundFailedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailOutboundRequestedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailOutboundSentEvent
import com.base.armsupportservice.service.EmailEventService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Подписка ARM на все email-топики от email-integration-service.
 * Value — сырая строка (snake_case JSON), десериализуется вручную через ObjectMapper.
 * Отдельная фабрика [emailKafkaListenerContainerFactory] — без type-headers.
 */
@Component
@ConditionalOnProperty(
    name = ["app.email-integration.kafka-consumers-enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class EmailKafkaListener(
    private val objectMapper: ObjectMapper,
    private val emailEventService: EmailEventService,
) {
    private val log = LoggerFactory.getLogger(EmailKafkaListener::class.java)

    @KafkaListener(
        topics = ["\${app.kafka.topics.email-conversation-created}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "emailKafkaListenerContainerFactory",
    )
    fun onConversationCreated(payload: String) {
        handle(payload, "email.conversation.created") {
            emailEventService.handleConversationCreated(
                objectMapper.readValue(payload, EmailConversationCreatedEvent::class.java),
            )
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.email-conversation-matched}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "emailKafkaListenerContainerFactory",
    )
    fun onConversationMatched(payload: String) {
        handle(payload, "email.conversation.matched") {
            emailEventService.handleConversationMatched(
                objectMapper.readValue(payload, EmailConversationMatchedEvent::class.java),
            )
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.email-inbound-persisted}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "emailKafkaListenerContainerFactory",
    )
    fun onInboundPersisted(payload: String) {
        handle(payload, "email.inbound.persisted") {
            emailEventService.handleInboundPersisted(
                objectMapper.readValue(payload, EmailInboundPersistedEvent::class.java),
            )
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.email-outbound-requested}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "emailKafkaListenerContainerFactory",
    )
    fun onOutboundRequested(payload: String) {
        handle(payload, "email.outbound.requested") {
            emailEventService.handleOutboundRequested(
                objectMapper.readValue(payload, EmailOutboundRequestedEvent::class.java),
            )
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.email-outbound-sent}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "emailKafkaListenerContainerFactory",
    )
    fun onOutboundSent(payload: String) {
        handle(payload, "email.outbound.sent") {
            emailEventService.handleOutboundSent(
                objectMapper.readValue(payload, EmailOutboundSentEvent::class.java),
            )
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.email-outbound-failed}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "emailKafkaListenerContainerFactory",
    )
    fun onOutboundFailed(payload: String) {
        handle(payload, "email.outbound.failed") {
            emailEventService.handleOutboundFailed(
                objectMapper.readValue(payload, EmailOutboundFailedEvent::class.java),
            )
        }
    }

    private inline fun handle(
        payload: String,
        topic: String,
        block: () -> Unit,
    ) {
        try {
            block()
        } catch (ex: Exception) {
            log.error("Failed to process message from topic={}: payload={}", topic, payload, ex)
            throw ex
        }
    }
}
