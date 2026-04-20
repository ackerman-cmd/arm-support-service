package com.base.armsupportservice.listener

import com.base.armsupportservice.integration.vk.dto.kafka.VkMessageFailedEvent
import com.base.armsupportservice.integration.vk.dto.kafka.VkMessageReceivedEvent
import com.base.armsupportservice.integration.vk.dto.kafka.VkMessageSentEvent
import com.base.armsupportservice.service.VkEventService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    name = ["app.vk-integration.kafka-consumers-enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class VkKafkaListener(
    private val objectMapper: ObjectMapper,
    private val vkEventService: VkEventService,
) {
    private val log = LoggerFactory.getLogger(VkKafkaListener::class.java)

    @KafkaListener(
        topics = ["\${app.kafka.topics.vk-message-received}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "vkKafkaListenerContainerFactory",
    )
    fun onMessageReceived(payload: String) {
        handle(payload, "vk.message.received") {
            vkEventService.handleMessageReceived(
                objectMapper.readValue(payload, VkMessageReceivedEvent::class.java),
            )
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.vk-message-sent}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "vkKafkaListenerContainerFactory",
    )
    fun onMessageSent(payload: String) {
        handle(payload, "vk.message.sent") {
            vkEventService.handleMessageSent(
                objectMapper.readValue(payload, VkMessageSentEvent::class.java),
            )
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.vk-message-failed}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "vkKafkaListenerContainerFactory",
    )
    fun onMessageFailed(payload: String) {
        handle(payload, "vk.message.failed") {
            vkEventService.handleMessageFailed(
                objectMapper.readValue(payload, VkMessageFailedEvent::class.java),
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
