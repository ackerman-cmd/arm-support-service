package com.base.armsupportservice.service

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealMessage
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.domain.appeal.AppealStatusMachine
import com.base.armsupportservice.domain.appeal.MessageSenderType
import com.base.armsupportservice.domain.user.UserStatus
import com.base.armsupportservice.dto.appeal.AppealActionsResponse
import com.base.armsupportservice.dto.appeal.AppealFilterRequest
import com.base.armsupportservice.dto.appeal.AppealMessageRequest
import com.base.armsupportservice.dto.appeal.AppealMessageResponse
import com.base.armsupportservice.dto.appeal.AppealRequest
import com.base.armsupportservice.dto.appeal.AppealResponse
import com.base.armsupportservice.dto.appeal.AppealUpdateRequest
import com.base.armsupportservice.dto.appeal.AssignOperatorRequest
import com.base.armsupportservice.dto.common.OperatorSummaryResponse
import com.base.armsupportservice.exception.AppealNotFoundException
import com.base.armsupportservice.exception.GroupNotFoundException
import com.base.armsupportservice.exception.OperatorNotFoundException
import com.base.armsupportservice.exception.OrganizationNotFoundException
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
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AppealService(
    private val appealRepository: AppealRepository,
    private val appealMessageRepository: AppealMessageRepository,
    private val organizationRepository: OrganizationRepository,
    private val assignmentGroupRepository: AssignmentGroupRepository,
    private val skillGroupRepository: SkillGroupRepository,
    private val syncedUserRepository: SyncedUserRepository,
    private val appealTopicRepository: AppealTopicRepository,
    private val permissionService: PermissionService,
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

        return toResponse(appealRepository.save(appeal))
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

        return toResponse(appealRepository.save(appeal))
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

        appeal.status = AppealStatus.IN_PROGRESS
        return toResponse(appealRepository.save(appeal))
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
            }

            request.assignmentGroupId != null -> {
                if (!assignmentGroupRepository.existsById(request.assignmentGroupId)) {
                    throw GroupNotFoundException(request.assignmentGroupId)
                }
                appeal.assignmentGroupId = request.assignmentGroupId
                appeal.assignedOperatorId = null
                if (request.clearActiveOperators) appeal.activeOperatorIds.clear()
            }

            request.skillGroupId != null -> {
                if (!skillGroupRepository.existsById(request.skillGroupId)) {
                    throw GroupNotFoundException(request.skillGroupId)
                }
                appeal.skillGroupId = request.skillGroupId
                appeal.assignedOperatorId = null
                if (request.clearActiveOperators) appeal.activeOperatorIds.clear()
            }
        }

        return toResponse(appealRepository.save(appeal))
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

        if (appeal.status == AppealStatus.PENDING_PROCESSING || appeal.status == AppealStatus.WAITING_CLIENT_RESPONSE) {
            appeal.status = AppealStatus.IN_PROGRESS
        }

        return toResponse(appealRepository.save(appeal))
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
        return toResponse(appealRepository.save(appeal))
    }

    @Transactional
    fun changeStatus(
        id: UUID,
        newStatus: AppealStatus,
    ): AppealResponse {
        val appeal = findOrThrow(id)
        AppealStatusMachine.validate(appeal.status, newStatus)
        appeal.status = newStatus
        if (newStatus == AppealStatus.CLOSED) {
            appeal.closedAt = LocalDateTime.now()
        }
        return toResponse(appealRepository.save(appeal))
    }

    @Transactional
    fun markAsSpam(id: UUID): AppealResponse = changeStatus(id, AppealStatus.SPAM)

    @Transactional
    fun close(id: UUID): AppealResponse = changeStatus(id, AppealStatus.CLOSED)

    @Transactional
    fun sendOperatorMessage(
        id: UUID,
        request: AppealMessageRequest,
        principal: UserPrincipal,
    ): AppealMessageResponse {
        val appeal = findOrThrow(id)

        val nextStatus = AppealStatusMachine.afterOperatorReply(appeal.status)
        appeal.status = nextStatus

        val message =
            AppealMessage(
                appeal = appeal,
                senderId = principal.userId,
                senderType = MessageSenderType.OPERATOR,
                content = request.content,
                channel = request.channel,
                externalMessageId = request.externalMessageId,
            )

        appealRepository.save(appeal)
        val saved = appealMessageRepository.save(message)
        return toMessageResponse(saved)
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

        appealRepository.save(appeal)
        val saved = appealMessageRepository.save(message)
        return toMessageResponse(saved)
    }

    fun getMessages(
        id: UUID,
        pageable: org.springframework.data.domain.Pageable,
    ): Page<AppealMessageResponse> {
        if (!appealRepository.existsById(id)) throw AppealNotFoundException(id)
        return appealMessageRepository
            .findByAppealIdOrderByCreatedAtAsc(id, pageable)
            .map { toMessageResponse(it) }
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

    private fun toMessageResponse(message: AppealMessage): AppealMessageResponse =
        AppealMessageResponse.from(message, resolveOperator(message.senderId))

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
