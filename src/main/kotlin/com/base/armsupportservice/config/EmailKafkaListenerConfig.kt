package com.base.armsupportservice.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

/**
 * Отдельная фабрика для топиков email-integration: без JSON type headers,
 * payload — сырая строка (десериализация в listener через Jackson + @JsonProperty snake_case).
 */
@Configuration
@ConditionalOnProperty(
    name = ["app.email-integration.kafka-consumers-enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class EmailKafkaListenerConfig {
    private val log = LoggerFactory.getLogger(EmailKafkaListenerConfig::class.java)

    @Bean
    fun emailKafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
    ): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = consumerFactory
        factory.containerProperties.ackMode = ContainerProperties.AckMode.RECORD
        factory.setCommonErrorHandler(emailKafkaErrorHandler())
        return factory
    }

    private fun emailKafkaErrorHandler(): CommonErrorHandler =
        DefaultErrorHandler(
            { record, ex ->
                log.warn(
                    "Skipping invalid email Kafka message topic={}, offset={}: {}",
                    record.topic(),
                    record.offset(),
                    ex.message,
                )
            },
            FixedBackOff(RETRY_INTERVAL_MS, MAX_ATTEMPTS),
        )

    companion object {
        private const val RETRY_INTERVAL_MS = 1000L
        private const val MAX_ATTEMPTS = 3L
    }
}
