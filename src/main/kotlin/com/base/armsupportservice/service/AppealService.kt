package com.base.armsupportservice.service

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealEvent
import com.base.armsupportservice.domain.appeal.AppealEventType
import com.base.armsupportservice.domain.appeal.AppealMessage
import com.base.armsupportservice.domain.appeal.AppealMessageAttachment
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.domain.appeal.AppealStatusMachine
import com.base.armsupportservice.domain.appeal.MessageDeliveryStatus
import com.base.armsupportservice.domain.appeal.MessageSenderType
import com.base.armsupportservice.domain.user.UserStatus
import com.base.armsupportservice.dto.appeal.AppealActionsResponse
import com.base.armsupportservice.dto.appeal.AppealEventResponse
import com.base.armsupportservice.dto.appeal.AppealFilterRequest
import com.base.armsupportservice.dto.appeal.AppealMessageAttachmentResponse
import com.base.armsupportservice.dto.appeal.AppealMessageRequest
import com.base.armsupportservice.dto.appeal.AppealMessageResponse
import com.base.armsupportservice.dto.appeal.AppealRequest
import com.base.armsupportservice.dto.appeal.AppealResponse
import com.base.armsupportservice.dto.appeal.AppealUpdateRequest
import com.base.armsupportservice.dto.appeal.AssignOperatorRequest
import com.base.armsupportservice.dto.appeal.ChatMessageRequest
import com.base.armsupportservice.dto.appeal.EmailMessageRequest
import com.base.armsupportservice.dto.common.OperatorSummaryResponse
import com.base.armsupportservice.exception.AppealNotFoundException
import com.base.armsupportservice.exception.GroupNotFoundException
import com.base.armsupportservice.exception.OperatorNotFoundException
import com.base.armsupportservice.exception.OrganizationNotFoundException
import com.base.armsupportservice.integration.email.EmailIntegrationClient
import com.base.armsupportservice.integration.email.dto.http.ReplyEmailRequest
import com.base.armsupportservice.integration.email.dto.http.SendEmailRequest
import com.base.armsupportservice.integration.vk.VkIntegrationClient
import com.base.armsupportservice.repository.AppealEventRepository
import com.base.armsupportservice.repository.AppealMessageAttachmentRepository
import com.base.armsupportservice.repository.AppealMessageRepository
import com.base.armsupportservice.repository.AppealRepository
import com.base.armsupportservice.repository.AppealSpecification
import com.base.armsupportservice.repository.AppealTopicRepository
import com.base.armsupportservice.repository.AssignmentGroupRepository
import com.base.armsupportservice.repository.OrganizationRepository
import com.base.armsupportservice.repository.SkillGroupRepository
import com.base.armsupportservice.repository.SyncedUserRepository
import com.base.armsupportservice.security.UserPrincipal
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AppealService(
    private val appealRepository: AppealRepository,
    private val appealMessageRepository: AppealMessageRepository,
    private val appealMessageAttachmentRepository: AppealMessageAttachmentRepository,
    private val appealEventRepository: AppealEventRepository,
    private val organizationRepository: OrganizationRepository,
    private val assignmentGroupRepository: AssignmentGroupRepository,
    private val skillGroupRepository: SkillGroupRepository,
    private val syncedUserRepository: SyncedUserRepository,
    private val appealTopicRepository: AppealTopicRepository,
    private val permissionService: PermissionService,
    private val emailIntegrationClient: EmailIntegrationClient,
    private val vkIntegrationClient: VkIntegrationClient,
) {
    fun getById(id: UUID): AppealResponse {
        val appeal = findOrThrow(id)
        return toResponse(appeal)
    }

    fun filter(filter: AppealFilterRequest): Page<AppealResponse> {
        val direction =
            if (filter.sortDirection.uppercase() == "ASC") {
                Sort.Direction.ASC
            } else {
                Sort.Direction.DESC
            }
        val pageable = PageRequest.of(filter.page, filter.size, Sort.by(direction, filter.sortBy))
        val spec = AppealSpecification.byFilter(filter)
        return appealRepository.findAll(spec, pageable).map { toResponse(it) }
    }

    @Transactional
    fun create(
        request: AppealRequest,
        principal: UserPrincipal,
    ): AppealResponse {
        val initialStatus = AppealStatusMachine.initialStatus(request.direction)

        val appeal =
            Appeal(
                subject = request.subject,
                description = request.description,
                channel = request.channel,
                direction = request.direction,
                status = initialStatus,
                priority = request.priority,
                organizationId = request.organizationId,
                topicId = request.topicId,
                contactName = request.contactName,
                contactEmail = request.contactEmail,
                contactPhone = request.contactPhone,
                assignedOperatorId = request.assignedOperatorId,
                assignmentGroupId = request.assignmentGroupId,
                skillGroupId = request.skillGroupId,
                createdById = principal.userId,
            )

        validateReferences(appeal)

        val saved = appealRepository.save(appeal)
        appealEventRepository.save(
            AppealEvent(
                appeal = saved,
                eventType = AppealEventType.CREATED,
                initiatorId = principal.userId,
                toStatus = initialStatus,
            ),
        )
        return toResponse(saved)
    }

    @Transactional
    fun update(
        id: UUID,
        request: AppealUpdateRequest,
    ): AppealResponse {
        val appeal = findOrThrow(id)

        request.subject?.let { appeal.subject = it }
        request.description?.let { appeal.description = it }
        request.channel?.let { appeal.channel = it }
        request.priority?.let { appeal.priority = it }
        request.contactName?.let { appeal.contactName = it }
        request.contactEmail?.let { appeal.contactEmail = it }
        request.contactPhone?.let { appeal.contactPhone = it }

        if (request.organizationId != null) {
            if (!organizationRepository.existsById(request.organizationId)) {
                throw OrganizationNotFoundException(request.organizationId)
            }
            appeal.organizationId = request.organizationId
        }
        if (request.topicId != null) {
            if (!appealTopicRepository.existsById(request.topicId)) throw GroupNotFoundException(request.topicId)
            appeal.topicId = request.topicId
        }
        if (request.assignmentGroupId != null) {
            if (!assignmentGroupRepository.existsById(request.assignmentGroupId)) {
                throw GroupNotFoundException(request.assignmentGroupId)
            }
            appeal.assignmentGroupId = request.assignmentGroupId
        }
        if (request.skillGroupId != null) {
            if (!skillGroupRepository.existsById(request.skillGroupId)) {
                throw GroupNotFoundException(request.skillGroupId)
            }
            appeal.skillGroupId = request.skillGroupId
        }

        val saved = appealRepository.save(appeal)
        appealEventRepository.save(
            AppealEvent(
                appeal = saved,
                eventType = AppealEventType.UPDATED,
            ),
        )
        return toResponse(saved)
    }

    @Transactional
    fun takeIntoWork(
        id: UUID,
        principal: UserPrincipal,
    ): AppealResponse {
        val appeal = findOrThrow(id)
        AppealStatusMachine.validate(appeal.status, AppealStatus.IN_PROGRESS)

        if (appeal.assignmentGroupId != null || appeal.skillGroupId != null) {
            // Групповое назначение — оператор присоединяется к активным участникам
            appeal.activeOperatorIds.add(principal.userId)
        } else {
            // Прямое назначение — становится единственным ответственным
            appeal.assignedOperatorId = principal.userId
            appeal.activeOperatorIds.clear()
            appeal.activeOperatorIds.add(principal.userId)
        }

        val prevStatus = appeal.status
        appeal.status = AppealStatus.IN_PROGRESS
        val saved = appealRepository.save(appeal)
        appealEventRepository.save(
            AppealEvent(
                appeal = saved,
                eventType = AppealEventType.STATUS_CHANGED,
                initiatorId = principal.userId,
                fromStatus = prevStatus,
                toStatus = AppealStatus.IN_PROGRESS,
            ),
        )
        return toResponse(saved)
    }

    @Transactional
    fun assign(
        id: UUID,
        request: AssignOperatorRequest,
    ): AppealResponse {
        val appeal = findOrThrow(id)
        if (appeal.status in setOf(AppealStatus.CLOSED, AppealStatus.SPAM)) {
            throw IllegalStateException("Нельзя переназначить закрытое или помеченное как спам обращение")
        }

        var assignEventType = AppealEventType.ASSIGNED_OPERATOR
        var assignComment: String? = null

        when {
            request.operatorId != null -> {
                val operator =
                    syncedUserRepository
                        .findById(request.operatorId)
                        .orElseThrow { OperatorNotFoundException(request.operatorId) }
                if (operator.status != UserStatus.ACTIVE) {
                    throw IllegalStateException("Нельзя назначить обращение неактивному оператору: ${operator.status}")
                }
                appeal.assignedOperatorId = request.operatorId
                appeal.assignmentGroupId = null
                appeal.skillGroupId = null
                if (request.clearActiveOperators) appeal.activeOperatorIds.clear()
                appeal.activeOperatorIds.add(request.operatorId)
                if (appeal.status == AppealStatus.PENDING_PROCESSING) appeal.status = AppealStatus.IN_PROGRESS
                assignEventType = AppealEventType.ASSIGNED_OPERATOR
                assignComment = listOfNotNull(operator.firstName, operator.lastName).joinToString(" ").ifBlank { operator.username }
            }

            request.assignmentGroupId != null -> {
                val group =
                    assignmentGroupRepository
                        .findById(request.assignmentGroupId)
                        .orElseThrow { GroupNotFoundException(request.assignmentGroupId) }
                appeal.assignmentGroupId = request.assignmentGroupId
                appeal.assignedOperatorId = null
                if (request.clearActiveOperators) appeal.activeOperatorIds.clear()
                assignEventType = AppealEventType.ASSIGNED_GROUP
                assignComment = group.name
            }

            request.skillGroupId != null -> {
                val group =
                    skillGroupRepository
                        .findById(request.skillGroupId)
                        .orElseThrow { GroupNotFoundException(request.skillGroupId) }
                appeal.skillGroupId = request.skillGroupId
                appeal.assignedOperatorId = null
                if (request.clearActiveOperators) appeal.activeOperatorIds.clear()
                assignEventType = AppealEventType.ASSIGNED_GROUP
                assignComment = group.name
            }
        }

        val saved = appealRepository.save(appeal)
        appealEventRepository.save(
            AppealEvent(
                appeal = saved,
                eventType = assignEventType,
                comment = assignComment,
            ),
        )
        return toResponse(saved)
    }

    /**
     * Оператор присоединяется к работе над групповым обращением.
     * Добавляет его в activeOperators и переводит в IN_PROGRESS если ещё не в работе.
     */
    @Transactional
    fun joinWork(
        id: UUID,
        principal: UserPrincipal,
    ): AppealResponse {
        val appeal = findOrThrow(id)

        if (appeal.assignmentGroupId == null && appeal.skillGroupId == null) {
            throw IllegalStateException("Присоединиться можно только к обращению, назначенному на группу")
        }
        if (appeal.status in setOf(AppealStatus.CLOSED, AppealStatus.SPAM)) {
            throw IllegalStateException("Нельзя присоединиться к закрытому обращению")
        }

        appeal.activeOperatorIds.add(principal.userId)

        val prevStatus = appeal.status
        if (appeal.status == AppealStatus.PENDING_PROCESSING || appeal.status == AppealStatus.WAITING_CLIENT_RESPONSE) {
            appeal.status = AppealStatus.IN_PROGRESS
        }

        val saved = appealRepository.save(appeal)
        appealEventRepository.save(
            AppealEvent(
                appeal = saved,
                eventType = AppealEventType.OPERATOR_JOINED,
                initiatorId = principal.userId,
                fromStatus = if (prevStatus != saved.status) prevStatus else null,
                toStatus = if (prevStatus != saved.status) saved.status else null,
            ),
        )
        return toResponse(saved)
    }

    /**
     * Оператор покидает работу над обращением.
     * Удаляет его из activeOperators. Статус обращения не меняется.
     */
    @Transactional
    fun leaveWork(
        id: UUID,
        principal: UserPrincipal,
    ): AppealResponse {
        val appeal = findOrThrow(id)
        appeal.activeOperatorIds.remove(principal.userId)
        val saved = appealRepository.save(appeal)
        appealEventRepository.save(
            AppealEvent(
                appeal = saved,
                eventType = AppealEventType.OPERATOR_LEFT,
                initiatorId = principal.userId,
            ),
        )
        return toResponse(saved)
    }

    @Transactional
    fun changeStatus(
        id: UUID,
        newStatus: AppealStatus,
        initiatorId: UUID? = null,
    ): AppealResponse {
        val appeal = findOrThrow(id)
        val prevStatus = appeal.status
        AppealStatusMachine.validate(prevStatus, newStatus)
        appeal.status = newStatus
        if (newStatus == AppealStatus.CLOSED) {
            appeal.closedAt = LocalDateTime.now()
        }
        val saved = appealRepository.save(appeal)
        appealEventRepository.save(
            AppealEvent(
                appeal = saved,
                eventType = AppealEventType.STATUS_CHANGED,
                initiatorId = initiatorId,
                fromStatus = prevStatus,
                toStatus = newStatus,
            ),
        )
        return toResponse(saved)
    }

    @Transactional
    fun markAsSpam(id: UUID): AppealResponse = changeStatus(id, AppealStatus.SPAM)

    @Transactional
    fun close(id: UUID): AppealResponse = changeStatus(id, AppealStatus.CLOSED)

    // ── EMAIL channel ─────────────────────────────────────────────────────────

    @Transactional
    fun sendEmailMessage(
        id: UUID,
        request: EmailMessageRequest,
        principal: UserPrincipal,
    ): AppealMessageResponse {
        val appeal = findOrThrow(id)
        val recipient =
            appeal.contactEmail?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("У обращения ${appeal.id} не указан contactEmail")

        val prevStatus = appeal.status
        val nextStatus = AppealStatusMachine.afterOperatorReply(appeal.status)
        appeal.status = nextStatus

        val created =
            if (appeal.emailConversationId == null) {
                val fromEmail =
                    request.fromEmail?.takeIf { it.isNotBlank() }
                        ?: throw IllegalArgumentException(
                            "fromEmail обязателен для первого письма (у обращения нет emailConversationId)",
                        )
                emailIntegrationClient.sendEmail(
                    SendEmailRequest(
                        fromEmail = fromEmail,
                        to = listOf(recipient),
                        subject = appeal.subject,
                        htmlBody = request.htmlContent,
                        textBody = request.content.ifBlank { null },
                        createdByUserId = principal.userId,
                    ),
                )
            } else {
                emailIntegrationClient.replyEmail(
                    ReplyEmailRequest(
                        conversationId = appeal.emailConversationId!!,
                        to = listOf(recipient),
                        htmlBody = request.htmlContent,
                        textBody = request.content.ifBlank { null },
                        createdByUserId = principal.userId,
                    ),
                )
            }

        if (appeal.emailConversationId == null) {
            appeal.emailConversationId = created.conversationId
        }

        val message =
            AppealMessage(
                appeal = appeal,
                senderId = principal.userId,
                senderType = MessageSenderType.OPERATOR,
                content = request.content.ifBlank { "(html-письмо)" },
                channel = AppealChannel.EMAIL,
                externalMessageId = created.messageId.toString(),
                deliveryStatus = null,
            )

        val savedAppeal = appealRepository.save(appeal)
        val saved = appealMessageRepository.save(message)
        appealEventRepository.save(
            AppealEvent(
                appeal = savedAppeal,
                eventType = AppealEventType.MESSAGE_SENT,
                initiatorId = principal.userId,
                fromStatus = if (prevStatus != nextStatus) prevStatus else null,
                toStatus = if (prevStatus != nextStatus) nextStatus else null,
            ),
        )
        return toMessageResponse(saved)
    }

    @Transactional
    fun sendEmailMessageWithAttachment(
        id: UUID,
        content: String,
        fromEmail: String?,
        htmlContent: String?,
        file: MultipartFile,
        principal: UserPrincipal,
    ): AppealMessageResponse {
        val appeal = findOrThrow(id)
        val recipient =
            appeal.contactEmail?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("У обращения ${appeal.id} не указан contactEmail")

        val prevStatus = appeal.status
        val nextStatus = AppealStatusMachine.afterOperatorReply(appeal.status)
        appeal.status = nextStatus

        val fileName = file.originalFilename ?: file.name

        val created =
            if (appeal.emailConversationId == null) {
                val sender =
                    fromEmail?.takeIf { it.isNotBlank() }
                        ?: throw IllegalArgumentException("fromEmail обязателен для первого письма")
                emailIntegrationClient.sendEmailWithAttachment(
                    fromEmail = sender,
                    to = listOf(recipient),
                    subject = appeal.subject,
                    textBody = content.ifBlank { null },
                    htmlBody = htmlContent,
                    createdByUserId = principal.userId,
                    file = file,
                )
            } else {
                emailIntegrationClient.replyEmailWithAttachment(
                    conversationId = appeal.emailConversationId!!,
                    to = listOf(recipient),
                    textBody = content.ifBlank { null },
                    htmlBody = htmlContent,
                    createdByUserId = principal.userId,
                    file = file,
                )
            }

        if (appeal.emailConversationId == null) {
            appeal.emailConversationId = created.conversationId
        }

        val message =
            AppealMessage(
                appeal = appeal,
                senderId = principal.userId,
                senderType = MessageSenderType.OPERATOR,
                content = content.ifBlank { "(вложение: $fileName)" },
                channel = AppealChannel.EMAIL,
                externalMessageId = created.messageId.toString(),
                deliveryStatus = null,
            )
        val savedAppeal = appealRepository.save(appeal)
        val savedMessage = appealMessageRepository.save(message)
        appealEventRepository.save(
            AppealEvent(
                appeal = savedAppeal,
                eventType = AppealEventType.MESSAGE_SENT,
                initiatorId = principal.userId,
                fromStatus = if (prevStatus != nextStatus) prevStatus else null,
                toStatus = if (prevStatus != nextStatus) nextStatus else null,
            ),
        )
        return AppealMessageResponse.from(savedMessage, resolveOperator(principal.userId), emptyList())
    }

    // ── CHAT (VK) channel ─────────────────────────────────────────────────────

    @Transactional
    fun sendChatMessage(
        id: UUID,
        request: ChatMessageRequest,
        principal: UserPrincipal,
    ): AppealMessageResponse {
        val appeal = findOrThrow(id)
        val peerId =
            appeal.vkPeerId
                ?: throw IllegalStateException(
                    "У обращения ${appeal.id} не указан vkPeerId — невозможно отправить VK сообщение",
                )

        val prevStatus = appeal.status
        val nextStatus = AppealStatusMachine.afterOperatorReply(appeal.status)
        appeal.status = nextStatus

        val sent = vkIntegrationClient.sendMessage(peerId, request.content)
        val externalMessageId = sent.vkMessageId?.toString()
        val deliveryStatus = if (sent.vkMessageId != null) MessageDeliveryStatus.SENT else MessageDeliveryStatus.PENDING

        val message =
            AppealMessage(
                appeal = appeal,
                senderId = principal.userId,
                senderType = MessageSenderType.OPERATOR,
                content = request.content,
                channel = AppealChannel.CHAT,
                externalMessageId = externalMessageId,
                deliveryStatus = deliveryStatus,
            )

        val savedAppeal = appealRepository.save(appeal)
        val saved = appealMessageRepository.save(message)
        appealEventRepository.save(
            AppealEvent(
                appeal = savedAppeal,
                eventType = AppealEventType.MESSAGE_SENT,
                initiatorId = principal.userId,
                fromStatus = if (prevStatus != nextStatus) prevStatus else null,
                toStatus = if (prevStatus != nextStatus) nextStatus else null,
            ),
        )
        return toMessageResponse(saved)
    }

    @Transactional
    fun sendChatMessageWithAttachment(
        id: UUID,
        content: String,
        file: MultipartFile,
        principal: UserPrincipal,
    ): AppealMessageResponse {
        val appeal = findOrThrow(id)
        val peerId =
            appeal.vkPeerId
                ?: throw IllegalStateException(
                    "У обращения ${appeal.id} не указан vkPeerId — невозможно отправить VK сообщение с вложением",
                )

        val prevStatus = appeal.status
        val nextStatus = AppealStatusMachine.afterOperatorReply(appeal.status)
        appeal.status = nextStatus

        val fileName = file.originalFilename ?: file.name
        val mimeType = file.contentType ?: "application/octet-stream"

        val sent =
            vkIntegrationClient.sendMessageWithAttachment(
                peerId = peerId,
                text = content,
                fileName = fileName,
                mimeType = mimeType,
                fileBytes = file.bytes,
            )

        val deliveryStatus = if (sent.vkMessageId != null) MessageDeliveryStatus.SENT else MessageDeliveryStatus.PENDING

        val message =
            AppealMessage(
                appeal = appeal,
                senderId = principal.userId,
                senderType = MessageSenderType.OPERATOR,
                content = content.ifBlank { "(вложение: $fileName)" },
                channel = AppealChannel.CHAT,
                externalMessageId = sent.vkMessageId?.toString(),
                deliveryStatus = deliveryStatus,
            )

        val savedAppeal = appealRepository.save(appeal)
        val savedMessage = appealMessageRepository.save(message)

        val attachments = mutableListOf<AppealMessageAttachment>()
        sent.attachment?.let { att ->
            attachments.add(
                appealMessageAttachmentRepository.save(
                    AppealMessageAttachment(
                        message = savedMessage,
                        attachmentType = att.type,
                        fileName = att.fileName,
                        mimeType = att.mimeType,
                        s3Key = att.s3Key,
                        s3Url = att.s3Url,
                        fileSize = att.fileSize,
                    ),
                ),
            )
        }

        appealEventRepository.save(
            AppealEvent(
                appeal = savedAppeal,
                eventType = AppealEventType.MESSAGE_SENT,
                initiatorId = principal.userId,
                fromStatus = if (prevStatus != nextStatus) prevStatus else null,
                toStatus = if (prevStatus != nextStatus) nextStatus else null,
            ),
        )

        return AppealMessageResponse.from(
            savedMessage,
            resolveOperator(principal.userId),
            attachments.map { AppealMessageAttachmentResponse.from(it) },
        )
    }

    @Transactional
    fun receiveClientMessage(
        id: UUID,
        request: AppealMessageRequest,
    ): AppealMessageResponse {
        val appeal = findOrThrow(id)

        if (request.externalMessageId != null &&
            appealMessageRepository.existsByExternalMessageId(request.externalMessageId)
        ) {
            return appealMessageRepository
                .findByAppealIdOrderByCreatedAtAsc(id, PageRequest.of(0, 1))
                .content
                .firstOrNull()
                ?.let { toMessageResponse(it) }
                ?: error("Дедупликация: сообщение найдено, но список пуст")
        }

        val prevStatus = appeal.status
        val nextStatus = AppealStatusMachine.afterClientReply(appeal.status)
        appeal.status = nextStatus

        val message =
            AppealMessage(
                appeal = appeal,
                senderId = null,
                senderType = MessageSenderType.CLIENT,
                content = request.content,
                channel = request.channel,
                externalMessageId = request.externalMessageId,
            )

        val savedAppeal = appealRepository.save(appeal)
        val saved = appealMessageRepository.save(message)
        appealEventRepository.save(
            AppealEvent(
                appeal = savedAppeal,
                eventType = AppealEventType.MESSAGE_RECEIVED,
                fromStatus = if (prevStatus != nextStatus) prevStatus else null,
                toStatus = if (prevStatus != nextStatus) nextStatus else null,
            ),
        )
        return toMessageResponse(saved)
    }

    fun getMessages(
        id: UUID,
        pageable: org.springframework.data.domain.Pageable,
    ): Page<AppealMessageResponse> {
        if (!appealRepository.existsById(id)) throw AppealNotFoundException(id)
        val page = appealMessageRepository.findByAppealIdOrderByCreatedAtAsc(id, pageable)
        val messageIds = page.content.map { it.id }
        val attachmentsByMessageId =
            appealMessageAttachmentRepository
                .findAllByMessageIdIn(messageIds)
                .groupBy { it.message.id }
        return page.map { msg ->
            val attachments =
                (attachmentsByMessageId[msg.id] ?: emptyList())
                    .map { AppealMessageAttachmentResponse.from(it) }
            AppealMessageResponse.from(msg, resolveOperator(msg.senderId), attachments)
        }
    }

    fun getActions(
        id: UUID,
        principal: UserPrincipal,
    ): AppealActionsResponse {
        val appeal = findOrThrow(id)
        val actionItems = permissionService.evaluateActions(appeal, principal)
        val transitions =
            (AppealStatusMachine.allowedTransitions[appeal.status] ?: emptySet())
                .map { s ->
                    AppealActionsResponse.StatusTransitionItem(
                        status = s,
                        label = STATUS_LABELS[s] ?: s.name,
                    )
                }
        return AppealActionsResponse(
            appealId = appeal.id,
            currentStatus = appeal.status,
            currentStatusLabel = STATUS_LABELS[appeal.status] ?: appeal.status.name,
            actions = actionItems,
            availableStatusTransitions = transitions,
        )
    }

    fun fetchByIds(ids: Set<UUID>): List<AppealResponse> = appealRepository.findAllById(ids).map { toResponse(it) }

    fun getEvents(
        id: UUID,
        pageable: org.springframework.data.domain.Pageable,
    ): Page<AppealEventResponse> {
        if (!appealRepository.existsById(id)) throw AppealNotFoundException(id)
        return appealEventRepository
            .findByAppealIdOrderByCreatedAtAsc(id, pageable)
            .map { event -> AppealEventResponse.from(event, resolveOperator(event.initiatorId), STATUS_LABELS) }
    }

    @Transactional
    fun delete(id: UUID) {
        if (!appealRepository.existsById(id)) throw AppealNotFoundException(id)
        appealRepository.deleteById(id)
    }

    private fun findOrThrow(id: UUID): Appeal = appealRepository.findById(id).orElseThrow { AppealNotFoundException(id) }

    private fun validateReferences(appeal: Appeal) {
        appeal.organizationId?.let { orgId ->
            if (!organizationRepository.existsById(orgId)) throw OrganizationNotFoundException(orgId)
        }
        appeal.assignmentGroupId?.let { groupId ->
            if (!assignmentGroupRepository.existsById(groupId)) throw GroupNotFoundException(groupId)
        }
        appeal.skillGroupId?.let { groupId ->
            if (!skillGroupRepository.existsById(groupId)) throw GroupNotFoundException(groupId)
        }
        appeal.topicId?.let { topicId ->
            if (!appealTopicRepository.existsById(topicId)) throw GroupNotFoundException(topicId)
        }
        appeal.assignedOperatorId?.let { opId ->
            if (!syncedUserRepository.existsById(opId)) throw OperatorNotFoundException(opId)
        }
    }

    private fun resolveOperator(operatorId: UUID?): OperatorSummaryResponse? =
        operatorId?.let { id ->
            syncedUserRepository.findById(id).orElse(null)?.let { user ->
                OperatorSummaryResponse(
                    id = user.id,
                    username = user.username,
                    email = user.email,
                    firstName = user.firstName,
                    lastName = user.lastName,
                )
            }
        }

    private fun toResponse(appeal: Appeal): AppealResponse {
        val assignedOperator = resolveOperator(appeal.assignedOperatorId)
        val activeOperators =
            if (appeal.activeOperatorIds.isEmpty()) {
                emptyList()
            } else {
                syncedUserRepository.findAllById(appeal.activeOperatorIds).map { user ->
                    OperatorSummaryResponse(
                        id = user.id,
                        username = user.username,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                    )
                }
            }
        return AppealResponse.from(appeal, assignedOperator, activeOperators)
    }

    private fun toMessageResponse(message: AppealMessage): AppealMessageResponse {
        val attachments =
            appealMessageAttachmentRepository
                .findAllByMessageId(message.id)
                .map { AppealMessageAttachmentResponse.from(it) }
        return AppealMessageResponse.from(message, resolveOperator(message.senderId), attachments)
    }

    companion object {
        val STATUS_LABELS =
            mapOf(
                AppealStatus.PENDING_PROCESSING to "Ожидает обработки",
                AppealStatus.IN_PROGRESS to "В работе",
                AppealStatus.WAITING_CLIENT_RESPONSE to "Ожидает ответа клиента",
                AppealStatus.CLOSED to "Закрыто",
                AppealStatus.SPAM to "Спам",
            )
    }
}
