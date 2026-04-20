package com.base.armsupportservice.integration.vk

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.vk-integration")
data class VkIntegrationProperties(
    var baseUrl: String = "http://localhost:8085",
    var connectTimeoutMillis: Long = 5_000,
    var readTimeoutMillis: Long = 15_000,
)
