package com.base.armsupportservice.service

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealMessage
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.domain.appeal.AppealStatusMachine
import com.base.armsupportservice.domain.appeal.MessageSenderType
import com.base.armsupportservice.exception.InvalidStatusTransitionException
import com.base.armsupportservice.integration.email.dto.kafka.EmailConversationCreatedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailConversationMatchedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailInboundPersistedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailOutboundFailedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailOutboundRequestedEvent
import com.base.armsupportservice.integration.email.dto.kafka.EmailOutboundSentEvent
import com.base.armsupportservice.repository.AppealMessageRepository
import com.base.armsupportservice.repository.AppealRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class EmailEventService(
    private val appealRepository: AppealRepository,
    private val appealMessageRepository: AppealMessageRepository,
) {
    private val log = LoggerFactory.getLogger(EmailEventService::class.java)

    /**
     * Новый email-тред. Создание обращения делается в handleInboundPersisted
     * по флагу is_new_conversation, поэтому здесь только журналируем.
     */
    fun handleConversationCreated(event: EmailConversationCreatedEvent) {
        log.info(
            "email.conversation.created: conversationId={}, fromEmail={}, subject={}",
            event.conversationId,
            event.fromEmail,
            event.subjectNormalized,
        )
    }

    /**
     * Email-сервис привязал входящее письмо к существующему треду.
     * Если appeal не найден по emailConversationId — журналируем предупреждение.
     */
    fun handleConversationMatched(event: EmailConversationMatchedEvent) {
        log.info(
            "email.conversation.matched: messageId={}, conversationId={}, caseId={}",
            event.messageId,
            event.conversationId,
            event.caseId,
        )
        val convId = runCatching { UUID.fromString(event.conversationId) }.getOrElse { return }
        if (appealRepository.findByEmailConversationId(convId) == null) {
            log.warn(
                "email.conversation.matched: no appeal for conversationId={}, appeal may be created later",
                event.conversationId,
            )
        }
    }

    /**
     * Входящее письмо сохранено в email-сервисе.
     *
     * - is_new_conversation=true → создаём новое обращение (channel=EMAIL, direction=INBOUND)
     *   и сохраняем CLIENT-сообщение.
     * - is_new_conversation=false → находим обращение по emailConversationId,
     *   обновляем статус и добавляем CLIENT-сообщение.
     * Дедупликация: пропускаем, если AppealMessage с externalMessageId уже существует.
     */
    @Transactional
    fun handleInboundPersisted(event: EmailInboundPersistedEvent) {
        if (appealMessageRepository.existsByExternalMessageId(event.messageId)) {
            log.debug("email.inbound.persisted: duplicate messageId={}, skipping", event.messageId)
            return
        }

        val convId =
            runCatching { UUID.fromString(event.conversationId) }.getOrElse {
                log.warn("email.inbound.persisted: invalid conversationId={}", event.conversationId)
                return
            }

        if (event.isNewConversation) {
            handleNewConversation(event, convId)
        } else {
            handleExistingConversation(event, convId)
        }
    }

    fun handleOutboundRequested(event: EmailOutboundRequestedEvent) {
        log.info(
            "email.outbound.requested: messageId={}, conversationId={}, toEmails={}",
            event.messageId,
            event.conversationId,
            event.toEmails,
        )
    }

    fun handleOutboundSent(event: EmailOutboundSentEvent) {
        log.info(
            "email.outbound.sent: messageId={}, conversationId={}, providerMessageId={}",
            event.messageId,
            event.conversationId,
            event.providerMessageId,
        )
    }

    fun handleOutboundFailed(event: EmailOutboundFailedEvent) {
        log.warn(
            "email.outbound.failed: messageId={}, conversationId={}, reason={}",
            event.messageId,
            event.conversationId,
            event.reason,
        )
    }

    private fun handleNewConversation(
        event: EmailInboundPersistedEvent,
        convId: UUID,
    ) {
        val existing = appealRepository.findByEmailConversationId(convId)
        if (existing != null) {
            log.warn(
                "email.inbound.persisted: appeal {} already linked to conversationId={}, adding message only",
                existing.id,
                convId,
            )
            saveClientMessage(existing, event.messageId, event.textBody, event.htmlBody, event.subject)
            return
        }

        val appeal =
            Appeal(
                subject = event.subject?.ifBlank { null } ?: "(без темы)",
                channel = AppealChannel.EMAIL,
                direction = AppealDirection.INBOUND,
                status = AppealStatus.PENDING_PROCESSING,
                contactEmail = event.fromEmail,
                emailConversationId = convId,
                createdById = SYSTEM_USER_ID,
            )
        appealRepository.save(appeal)
        saveClientMessage(appeal, event.messageId, event.textBody, event.htmlBody, event.subject)
        log.info(
            "email.inbound.persisted: created appeal id={} for conversationId={}",
            appeal.id,
            convId,
        )
    }

    private fun handleExistingConversation(
        event: EmailInboundPersistedEvent,
        convId: UUID,
    ) {
        val appeal = appealRepository.findByEmailConversationId(convId)
        if (appeal == null) {
            log.warn(
                "email.inbound.persisted: no appeal found for conversationId={}, cannot add message",
                convId,
            )
            return
        }

        try {
            appeal.status = AppealStatusMachine.afterClientReply(appeal.status)
            appealRepository.save(appeal)
        } catch (ex: InvalidStatusTransitionException) {
            log.debug(
                "email.inbound.persisted: appeal {} in status {}, status not updated",
                appeal.id,
                appeal.status,
            )
        }

        saveClientMessage(appeal, event.messageId, event.textBody, event.htmlBody, event.subject)
    }

    private fun saveClientMessage(
        appeal: Appeal,
        externalMessageId: String,
        textBody: String?,
        htmlBody: String?,
        subject: String?,
    ) {
        // Prefer plain text body; fall back to stripped HTML; then subject placeholder.
        val subjectFallback = subject?.ifBlank { null } ?: "(без темы)"
        val content =
            textBody?.ifBlank { null }
                ?: htmlBody?.let { stripHtmlTags(it) }?.ifBlank { null }
                ?: subjectFallback
        val message =
            AppealMessage(
                appeal = appeal,
                senderType = MessageSenderType.CLIENT,
                content = content,
                channel = AppealChannel.EMAIL,
                externalMessageId = externalMessageId,
            )
        appealMessageRepository.save(message)
    }

    /** Removes HTML tags for storing a readable plain-text snapshot of the email body. */
    private fun stripHtmlTags(html: String): String =
        html
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()

    companion object {
        /** Системный пользователь — автор обращений, созданных автоматически из входящей почты. */
        val SYSTEM_USER_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    }
}
