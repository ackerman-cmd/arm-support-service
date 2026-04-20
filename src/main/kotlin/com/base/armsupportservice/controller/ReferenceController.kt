package com.base.armsupportservice.controller

import com.base.armsupportservice.domain.appeal.AppealAction
import com.base.armsupportservice.domain.appeal.AppealChannel
import com.base.armsupportservice.domain.appeal.AppealDirection
import com.base.armsupportservice.domain.appeal.AppealPriority
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.domain.appeal.AppealStatusMachine
import com.base.armsupportservice.domain.topic.AppealTopicCategory
import com.base.armsupportservice.dto.group.AllGroupsWithOperatorsResponse
import com.base.armsupportservice.dto.reference.ReferenceItemResponse
import com.base.armsupportservice.dto.reference.StatusTransitionMatrixResponse
import com.base.armsupportservice.dto.topic.AppealTopicsByCategoryResponse
import com.base.armsupportservice.service.AppealTopicService
import com.base.armsupportservice.service.GroupReferenceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/references")
@Tag(
    name = "References",
    description = "Справочники — энумы, матрица переходов, тематики. Используются фронтом для заполнения фильтров и форм.",
)
class ReferenceController(
    private val topicService: AppealTopicService,
    private val groupReferenceService: GroupReferenceService,
) {
    @GetMapping("/appeal-statuses")
    @Operation(summary = "Список статусов обращений с метками")
    fun appealStatuses(): List<ReferenceItemResponse> =
        AppealStatus.entries.map {
            ReferenceItemResponse(
                code = it.name,
                label = STATUS_LABELS[it] ?: it.name,
                description = STATUS_DESCRIPTIONS[it],
            )
        }

    @GetMapping("/appeal-channels")
    @Operation(summary = "Список каналов обращений")
    fun appealChannels(): List<ReferenceItemResponse> =
        AppealChannel.entries.map {
            ReferenceItemResponse(
                code = it.name,
                label = CHANNEL_LABELS[it] ?: it.name,
            )
        }

    @GetMapping("/appeal-directions")
    @Operation(summary = "Список направлений обращений")
    fun appealDirections(): List<ReferenceItemResponse> =
        AppealDirection.entries.map {
            ReferenceItemResponse(
                code = it.name,
                label = if (it == AppealDirection.INBOUND) "Входящее" else "Исходящее",
            )
        }

    @GetMapping("/appeal-priorities")
    @Operation(summary = "Список приоритетов обращений")
    fun appealPriorities(): List<ReferenceItemResponse> =
        AppealPriority.entries.map {
            ReferenceItemResponse(
                code = it.name,
                label = PRIORITY_LABELS[it] ?: it.name,
            )
        }

    @GetMapping("/appeal-actions")
    @Operation(summary = "Список всех действий над обращениями с описаниями")
    fun appealActions(): List<ReferenceItemResponse> =
        AppealAction.entries.map {
            ReferenceItemResponse(code = it.name, label = it.label, description = it.description)
        }

    @GetMapping("/appeal-topic-categories")
    @Operation(summary = "Список категорий тематик")
    fun topicCategories(): List<ReferenceItemResponse> =
        AppealTopicCategory.entries.map {
            ReferenceItemResponse(code = it.name, label = it.label)
        }

    @GetMapping("/appeal-topics")
    @Operation(summary = "Активные тематики, сгруппированные по категории — для dropdown при создании/редактировании обращения")
    fun topicsGrouped(): List<AppealTopicsByCategoryResponse> = topicService.getGroupedByCategory()

    @GetMapping("/status-transitions")
    @Operation(
        summary = "Матрица допустимых переходов между статусами",
        description =
            "Для каждого статуса возвращает список статусов, в которые можно перейти. " +
                "Используется фронтом для формирования доступных действий.",
    )
    fun statusTransitionMatrix(): StatusTransitionMatrixResponse =
        StatusTransitionMatrixResponse(
            matrix = AppealStatusMachine.allowedTransitions.mapValues { it.value.toList() },
            statuses =
                AppealStatus.entries.associateWith { status ->
                    StatusTransitionMatrixResponse.StatusMeta(
                        label = STATUS_LABELS[status] ?: status.name,
                        description = STATUS_DESCRIPTIONS[status] ?: "",
                        terminal = status == AppealStatus.CLOSED,
                    )
                },
        )

    @GetMapping("/groups-with-operators")
    @Operation(
        summary = "Получить все группы (назначения и скилл) с их операторами",
        description = "Используется для выбора оператора и группы при назначении обращения на фронтенде.",
    )
    fun groupsWithOperators(): AllGroupsWithOperatorsResponse = groupReferenceService.getAllGroupsWithOperators()

    companion object {
        val STATUS_LABELS =
            mapOf(
                AppealStatus.PENDING_PROCESSING to "Ожидает обработки",
                AppealStatus.IN_PROGRESS to "В работе",
                AppealStatus.WAITING_CLIENT_RESPONSE to "Ожидает ответа клиента",
                AppealStatus.CLOSED to "Закрыто",
                AppealStatus.SPAM to "Спам",
            )
        val STATUS_DESCRIPTIONS =
            mapOf(
                AppealStatus.PENDING_PROCESSING to "Входящее обращение поступило и ожидает назначения оператора",
                AppealStatus.IN_PROGRESS to "Оператор взял обращение в работу",
                AppealStatus.WAITING_CLIENT_RESPONSE to "Оператор ответил — ожидается ответ от клиента",
                AppealStatus.CLOSED to "Работа по обращению завершена",
                AppealStatus.SPAM to "Обращение помечено как нежелательное",
            )
        val CHANNEL_LABELS =
            mapOf(
                AppealChannel.EMAIL to "Электронная почта",
                AppealChannel.CHAT to "ВКонтакте",
            )
        val PRIORITY_LABELS =
            mapOf(
                AppealPriority.LOW to "Низкий",
                AppealPriority.MEDIUM to "Средний",
                AppealPriority.HIGH to "Высокий",
                AppealPriority.CRITICAL to "Критический",
            )
    }
}
