package com.base.armsupportservice.integration.vk.dto.kafka

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class VkMessageSentEvent(
    @JsonProperty("messageId") val messageId: UUID,
    @JsonProperty("conversationId") val conversationId: UUID,
    @JsonProperty("peerId") val peerId: Long,
    @JsonProperty("text") val text: String,
    @JsonProperty("vkMessageId") val vkMessageId: Long?,
    @JsonProperty("sentAt") val sentAt: Instant,
)
