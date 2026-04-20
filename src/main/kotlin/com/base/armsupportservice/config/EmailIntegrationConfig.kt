package com.base.armsupportservice.config

import com.base.armsupportservice.integration.email.EmailIntegrationProperties
import com.base.armsupportservice.integration.vk.VkIntegrationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(EmailIntegrationProperties::class, KafkaTopicsProperties::class, VkIntegrationProperties::class)
class EmailIntegrationConfig {
    @Bean("emailIntegrationRestClient")
    fun emailIntegrationRestClient(props: EmailIntegrationProperties): RestClient {
        val factory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(props.connectTimeoutMillis.toInt())
                setReadTimeout(props.readTimeoutMillis.toInt())
            }
        return RestClient
            .builder()
            .baseUrl(props.baseUrl.trimEnd('/'))
            .requestFactory(factory)
            .build()
    }

    @Bean("vkIntegrationRestClient")
    fun vkIntegrationRestClient(props: VkIntegrationProperties): RestClient {
        val factory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(props.connectTimeoutMillis.toInt())
                setReadTimeout(props.readTimeoutMillis.toInt())
            }
        return RestClient
            .builder()
            .baseUrl(props.baseUrl.trimEnd('/'))
            .requestFactory(factory)
            .build()
    }
}
