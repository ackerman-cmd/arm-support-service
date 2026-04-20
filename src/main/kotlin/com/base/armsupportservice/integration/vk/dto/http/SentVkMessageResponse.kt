package com.base.armsupportservice.integration.vk.dto.http

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SentVkMessageResponse(
    val vkMessageId: Long?,
    val attachment: SentAttachmentInfo? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SentAttachmentInfo(
    val type: String,
    val fileName: String,
    val mimeType: String,
    val s3Key: String,
    val s3Url: String,
    val fileSize: Long?,
)
