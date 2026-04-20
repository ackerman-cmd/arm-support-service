package com.base.armsupportservice.integration.vk.dto.kafka

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class VkMessageReceivedEvent(
    @JsonProperty("messageId") val messageId: UUID,
    @JsonProperty("conversationId") val conversationId: UUID,
    @JsonProperty("peerId") val peerId: Long,
    @JsonProperty("fromId") val fromId: Long,
    @JsonProperty("text") val text: String,
    @JsonProperty("vkMessageId") val vkMessageId: Long?,
    @JsonProperty("isNewConversation") val isNewConversation: Boolean,
    @JsonProperty("receivedAt") val receivedAt: Instant,
    @JsonProperty("attachments") val attachments: List<VkAttachmentInfo> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class VkAttachmentInfo(
    @JsonProperty("attachmentId") val attachmentId: UUID,
    @JsonProperty("type") val type: String,
    @JsonProperty("fileName") val fileName: String,
    @JsonProperty("mimeType") val mimeType: String,
    @JsonProperty("s3Key") val s3Key: String,
    @JsonProperty("s3Url") val s3Url: String,
    @JsonProperty("fileSize") val fileSize: Long?,
)
