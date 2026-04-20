package com.base.armsupportservice.integration.vk.dto.kafka

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class VkMessageFailedEvent(
    @JsonProperty("messageId") val messageId: UUID,
    @JsonProperty("conversationId") val conversationId: UUID,
    @JsonProperty("peerId") val peerId: Long,
    @JsonProperty("text") val text: String,
    @JsonProperty("errorMessage") val errorMessage: String?,
    @JsonProperty("failedAt") val failedAt: Instant,
)
