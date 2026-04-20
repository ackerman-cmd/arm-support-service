package com.base.armsupportservice.service

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealMessage
import com.base.armsupportservice.domain.appeal.AppealMessageAttachment
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.domain.appeal.AppealStatusMachine
import com.base.armsupportservice.domain.appeal.MessageDeliveryStatus
import com.base.armsupportservice.domain.appeal.MessageSenderType
import com.base.armsupportservice.exception.InvalidStatusTransitionException
import com.base.armsupportservice.integration.vk.dto.kafka.VkAttachmentInfo
import com.base.armsupportservice.integration.vk.dto.kafka.VkMessageFailedEvent
import com.base.armsupportservice.integration.vk.dto.kafka.VkMessageReceivedEvent
import com.base.armsupportservice.integration.vk.dto.kafka.VkMessageSentEvent
import com.base.armsupportservice.repository.AppealMessageAttachmentRepository
import com.base.armsupportservice.repository.AppealMessageRepository
import com.base.armsupportservice.repository.AppealRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VkEventService(
    private val appealRepository: AppealRepository,
    private val appealMessageRepository: AppealMessageRepository,
    private val appealMessageAttachmentRepository: AppealMessageAttachmentRepository,
) {
    private val log = LoggerFactory.getLogger(VkEventService::class.java)

    /**
     * Входящее VK-сообщение из vk_service.
     * Дедупликация по externalMessageId.
     */
    @Transactional
    fun handleMessageReceived(event: VkMessageReceivedEvent) {
        val externalId = event.vkMessageId?.toString() ?: event.messageId.toString()

        if (appealMessageRepository.existsByExternalMessageId(externalId)) {
            log.debug("vk.message.received: duplicate externalId={}, skipping", externalId)
            return
        }

        val message =
            if (event.isNewConversation) {
                handleNewConversation(event, externalId)
            } else {
                handleExistingConversation(event, externalId)
            }

        if (message != null && event.attachments.isNotEmpty()) {
            saveAttachments(event.attachments, message)
        }
    }

    /**
     * VK-сообщение от оператора подтверждено как отправленное.
     * Обновляем deliveryStatus → SENT.
     */
    @Transactional
    fun handleMessageSent(event: VkMessageSentEvent) {
        val externalId = event.vkMessageId?.toString() ?: return
        val message = appealMessageRepository.findByExternalMessageId(externalId)
        if (message == null) {
            log.debug("vk.message.sent: no AppealMessage found for externalId={}", externalId)
            return
        }
        if (message.deliveryStatus != MessageDeliveryStatus.SENT) {
            message.deliveryStatus = MessageDeliveryStatus.SENT
            appealMessageRepository.save(message)
            log.debug("vk.message.sent: updated delivery status for messageId={}", message.id)
        }
    }

    /**
     * Отправка VK-сообщения завершилась ошибкой.
     * Обновляем deliveryStatus → FAILED.
     */
    @Transactional
    fun handleMessageFailed(event: VkMessageFailedEvent) {
        val appeal =
            appealRepository.findActiveByVkPeerId(
                event.peerId,
                listOf(AppealStatus.CLOSED, AppealStatus.SPAM),
            ) ?: return

        val messages =
            appealMessageRepository.findByAppealIdAndDeliveryStatus(
                appeal.id,
                MessageDeliveryStatus.PENDING,
            )
        if (messages.isEmpty()) {
            log.debug("vk.message.failed: no PENDING messages for peerId={}", event.peerId)
            return
        }
        // Наиболее вероятно — последнее PENDING сообщение
        val message = messages.maxByOrNull { it.createdAt } ?: return
        message.deliveryStatus = MessageDeliveryStatus.FAILED
        appealMessageRepository.save(message)
        log.warn(
            "vk.message.failed: marked message {} as FAILED, error={}",
            message.id,
            event.errorMessage,
        )
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private fun handleNewConversation(
        event: VkMessageReceivedEvent,
        externalId: String,
    ): AppealMessage? {
        val existing =
            appealRepository.findActiveByVkPeerId(
                event.peerId,
                listOf(AppealStatus.CLOSED, AppealStatus.SPAM),
            )
        val appeal =
            if (existing != null) {
                log.warn(
                    "vk.message.received: active appeal {} already linked to peerId={}, adding message only",
                    existing.id,
                    event.peerId,
                )
                existing
            } else {
                val newAppeal =
                    Appeal(
                        subject = "VK сообщение от пользователя ${event.fromId}",
                        channel = AppealChannel.CHAT,
                        direction = AppealDirection.INBOUND,
                        status = AppealStatus.PENDING_PROCESSING,
                        vkPeerId = event.peerId,
                        createdById = SYSTEM_USER_ID,
                    )
                appealRepository.save(newAppeal).also {
                    log.info(
                        "vk.message.received: created appeal id={} for peerId={} fromId={}",
                        it.id,
                        event.peerId,
                        event.fromId,
                    )
                }
            }
        return saveClientMessage(appeal, externalId, event.text)
    }

    private fun handleExistingConversation(
        event: VkMessageReceivedEvent,
        externalId: String,
    ): AppealMessage? {
        val appeal =
            appealRepository.findActiveByVkPeerId(
                event.peerId,
                listOf(AppealStatus.CLOSED, AppealStatus.SPAM),
            )
        if (appeal == null) {
            log.warn(
                "vk.message.received: no active appeal for peerId={}, creating new one",
                event.peerId,
            )
            return handleNewConversation(event, externalId)
        }

        try {
            appeal.status = AppealStatusMachine.afterClientReply(appeal.status)
            appealRepository.save(appeal)
        } catch (ex: InvalidStatusTransitionException) {
            log.debug(
                "vk.message.received: appeal {} in status {}, status not updated",
                appeal.id,
                appeal.status,
            )
        }

        return saveClientMessage(appeal, externalId, event.text)
    }

    private fun saveClientMessage(
        appeal: Appeal,
        externalMessageId: String,
        text: String,
    ): AppealMessage =
        appealMessageRepository.save(
            AppealMessage(
                appeal = appeal,
                senderType = MessageSenderType.CLIENT,
                content = text.ifBlank { "(нет содержимого)" },
                channel = AppealChannel.CHAT,
                externalMessageId = externalMessageId,
            ),
        )

    private fun saveAttachments(
        attachments: List<VkAttachmentInfo>,
        message: AppealMessage,
    ) {
        attachments.forEach { att ->
            appealMessageAttachmentRepository.save(
                AppealMessageAttachment(
                    message = message,
                    attachmentType = att.type,
                    fileName = att.fileName,
                    mimeType = att.mimeType,
                    s3Key = att.s3Key,
                    s3Url = att.s3Url,
                    fileSize = att.fileSize,
                ),
            )
        }
        log.debug("Saved {} attachments for messageId={}", attachments.size, message.id)
    }

    companion object {
        val SYSTEM_USER_ID = EmailEventService.SYSTEM_USER_ID
    }
}
