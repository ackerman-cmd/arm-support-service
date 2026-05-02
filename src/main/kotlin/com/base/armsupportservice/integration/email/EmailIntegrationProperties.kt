package com.base.armsupportservice.integration.email

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.email-integration")
data class EmailIntegrationProperties(
    /** Базовый URL email-integration-service (без завершающего /) */
    var baseUrl: String = "http://localhost:8084",
    /** Включить Kafka consumer-ы событий почты (топики из раздела 3 контракта) */
    var kafkaConsumersEnabled: Boolean = true,
    var connectTimeoutMillis: Long = 5_000,
    var readTimeoutMillis: Long = 120_000,
)
