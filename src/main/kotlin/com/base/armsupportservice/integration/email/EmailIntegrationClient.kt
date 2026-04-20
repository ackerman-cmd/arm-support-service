package com.base.armsupportservice.integration.email

import com.base.armsupportservice.integration.email.dto.http.AttachmentResponse
import com.base.armsupportservice.integration.email.dto.http.ConversationResponse
import com.base.armsupportservice.integration.email.dto.http.ForwardEmailRequest
import com.base.armsupportservice.integration.email.dto.http.MessageCreatedResponse
import com.base.armsupportservice.integration.email.dto.http.MessageResponse
import com.base.armsupportservice.integration.email.dto.http.ReplyEmailRequest
import com.base.armsupportservice.integration.email.dto.http.SendEmailRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * HTTP-клиент к email-integration-service (контракт: ARM → почтовый сервис).
 * Базовый путь на стороне email-integration: `/internal/...`, JSON camelCase.
 */
@Service
class EmailIntegrationClient(
    @Qualifier("emailIntegrationRestClient") private val restClient: RestClient,
) {
    fun sendEmail(request: SendEmailRequest): MessageCreatedResponse =
        restClient
            .post()
            .uri("/internal/emails/send")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(MessageCreatedResponse::class.java)
            ?: throw IllegalStateException("Empty body from POST /internal/emails/send")

    fun replyEmail(request: ReplyEmailRequest): MessageCreatedResponse =
        restClient
            .post()
            .uri("/internal/emails/reply")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(MessageCreatedResponse::class.java)
            ?: throw IllegalStateException("Empty body from POST /internal/emails/reply")

    fun forwardEmail(request: ForwardEmailRequest): MessageCreatedResponse =
        restClient
            .post()
            .uri("/internal/emails/forward")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(MessageCreatedResponse::class.java)
            ?: throw IllegalStateException("Empty body from POST /internal/emails/forward")

    fun sendEmailWithAttachment(
        fromEmail: String,
        to: List<String>,
        subject: String,
        textBody: String?,
        htmlBody: String?,
        createdByUserId: UUID?,
        file: MultipartFile,
    ): MessageCreatedResponse {
        val form = LinkedMultiValueMap<String, Any>()
        form.add("fromEmail", fromEmail)
        to.forEach { form.add("to", it) }
        form.add("subject", subject)
        textBody?.let { form.add("textBody", it) }
        htmlBody?.let { form.add("htmlBody", it) }
        createdByUserId?.let { form.add("createdByUserId", it.toString()) }
        val fileName = file.originalFilename ?: file.name
        form.add(
            "files",
            object : ByteArrayResource(file.bytes) {
                override fun getFilename() = fileName
            },
        )
        return restClient
            .post()
            .uri("/internal/emails/send-with-attachments")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(form)
            .retrieve()
            .body(MessageCreatedResponse::class.java)
            ?: throw IllegalStateException("Empty body from POST /internal/emails/send-with-attachments")
    }

    fun replyEmailWithAttachment(
        conversationId: UUID,
        to: List<String>,
        textBody: String?,
        htmlBody: String?,
        createdByUserId: UUID?,
        file: MultipartFile,
    ): MessageCreatedResponse {
        val form = LinkedMultiValueMap<String, Any>()
        form.add("conversationId", conversationId.toString())
        to.forEach { form.add("to", it) }
        textBody?.let { form.add("textBody", it) }
        htmlBody?.let { form.add("htmlBody", it) }
        createdByUserId?.let { form.add("createdByUserId", it.toString()) }
        val fileName = file.originalFilename ?: file.name
        form.add(
            "files",
            object : ByteArrayResource(file.bytes) {
                override fun getFilename() = fileName
            },
        )
        return restClient
            .post()
            .uri("/internal/emails/reply-with-attachments")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(form)
            .retrieve()
            .body(MessageCreatedResponse::class.java)
            ?: throw IllegalStateException("Empty body from POST /internal/emails/reply-with-attachments")
    }

    fun getConversation(conversationId: UUID): ConversationResponse =
        restClient
            .get()
            .uri("/internal/conversations/{id}", conversationId)
            .retrieve()
            .body(ConversationResponse::class.java)
            ?: throw IllegalStateException("Empty body from GET /internal/conversations/{id}")

    fun getMessages(conversationId: UUID): List<MessageResponse> =
        restClient
            .get()
            .uri("/internal/conversations/{id}/messages", conversationId)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<MessageResponse>>() {})

            ?: emptyList()

    fun getAttachments(messageId: UUID): List<AttachmentResponse> =
        restClient
            .get()
            .uri("/internal/messages/{id}/attachments", messageId)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<AttachmentResponse>>() {})

            ?: emptyList()
}
