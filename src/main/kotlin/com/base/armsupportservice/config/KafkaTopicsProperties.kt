package com.base.armsupportservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kafka.topics")
data class KafkaTopicsProperties(
    var userSync: String = "user-sync",
    var emailConversationCreated: String = "email.conversation.created",
    var emailConversationMatched: String = "email.conversation.matched",
    var emailInboundPersisted: String = "email.inbound.persisted",
    var emailOutboundRequested: String = "email.outbound.requested",
    var emailOutboundSent: String = "email.outbound.sent",
    var emailOutboundFailed: String = "email.outbound.failed",
)
