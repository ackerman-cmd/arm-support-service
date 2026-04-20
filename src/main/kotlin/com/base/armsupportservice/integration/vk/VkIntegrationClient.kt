package com.base.armsupportservice.integration.vk

import com.base.armsupportservice.integration.vk.dto.http.SentVkMessageResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class VkIntegrationClient(
    @Qualifier("vkIntegrationRestClient") private val restClient: RestClient,
) {
    private val log = LoggerFactory.getLogger(VkIntegrationClient::class.java)

    fun sendMessage(
        peerId: Long,
        text: String,
    ): SentVkMessageResponse =
        try {
            restClient
                .post()
                .uri("/internal/vk/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("peerId" to peerId, "text" to text))
                .retrieve()
                .body(SentVkMessageResponse::class.java)
                ?: SentVkMessageResponse(vkMessageId = null)
        } catch (ex: RestClientResponseException) {
            log.error("vk_service error on sendMessage: {} — {}", ex.statusCode, ex.responseBodyAsString)
            throw ex
        }

    fun sendMessageWithAttachment(
        peerId: Long,
        text: String,
        fileName: String,
        mimeType: String,
        fileBytes: ByteArray,
    ): SentVkMessageResponse {
        val formData = LinkedMultiValueMap<String, Any>()
        formData.add("peerId", peerId.toString())
        if (text.isNotBlank()) formData.add("text", text)
        formData.add(
            "file",
            object : ByteArrayResource(fileBytes) {
                override fun getFilename() = fileName
            },
        )

        return try {
            restClient
                .post()
                .uri("/internal/vk/send-with-attachment")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(formData)
                .retrieve()
                .body(SentVkMessageResponse::class.java)
                ?: SentVkMessageResponse(vkMessageId = null)
        } catch (ex: RestClientResponseException) {
            log.error("vk_service error on sendWithAttachment peerId={}: {} — {}", peerId, ex.statusCode, ex.responseBodyAsString)
            throw ex
        }
    }
}
