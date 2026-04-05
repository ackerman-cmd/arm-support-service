package com.base.armsupportservice.config

import com.base.armsupportservice.integration.email.EmailIntegrationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(EmailIntegrationProperties::class, KafkaTopicsProperties::class)
class EmailIntegrationConfig {
    @Bean("emailIntegrationRestClient")
    fun emailIntegrationRestClient(props: EmailIntegrationProperties): RestClient {
        val factory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(props.connectTimeoutMillis.toInt())
                setReadTimeout(props.readTimeoutMillis.toInt())
            }
        val base = props.baseUrl.trimEnd('/')
        return RestClient
            .builder()
            .baseUrl(base)
            .requestFactory(factory)
            .build()
    }
}
