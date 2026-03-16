package com.base.armsupportservice.listener

import com.base.armsupportservice.event.UserSyncEvent
import com.base.armsupportservice.service.UserSyncService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@KafkaListener(
    topics = ["\${app.kafka.topics.user-sync}"],
    groupId = "\${spring.kafka.consumer.group-id}",
    containerFactory = "kafkaListenerContainerFactory",
)
class UserSyncListener(
    private val userSyncService: UserSyncService,
) {
    private val log = LoggerFactory.getLogger(UserSyncListener::class.java)

    @KafkaHandler
    fun onUserSync(event: UserSyncEvent) {
        log.debug("Received user-sync event: userId={}, type={}", event.userId, event.eventType)
        userSyncService.handleSync(event)
    }

    @KafkaHandler(isDefault = true)
    fun onUnknown(message: Any) {
        log.warn("Received unknown message on user-sync topic, skipping: {}", message)
    }
}
