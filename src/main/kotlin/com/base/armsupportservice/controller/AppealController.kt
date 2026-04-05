package com.base.armsupportservice.controller

import com.base.armsupportservice.dto.appeal.AppealActionsResponse
import com.base.armsupportservice.dto.appeal.AppealEventResponse
import com.base.armsupportservice.dto.appeal.AppealFilterRequest
import com.base.armsupportservice.dto.appeal.AppealMessageRequest
import com.base.armsupportservice.dto.appeal.AppealMessageResponse
import com.base.armsupportservice.dto.appeal.AppealRequest
import com.base.armsupportservice.dto.appeal.AppealResponse
import com.base.armsupportservice.dto.appeal.AppealUpdateRequest
import com.base.armsupportservice.dto.appeal.AssignOperatorRequest
import com.base.armsupportservice.dto.appeal.ChangeStatusRequest
import com.base.armsupportservice.dto.common.FetchByIdsRequest
import com.base.armsupportservice.security.UserPrincipal
import com.base.armsupportservice.service.AppealService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/appeals")
@Tag(name = "Appeals", description = "Управление обращениями")
class AppealController(
    private val appealService: AppealService,
) {
    @GetMapping
    @Operation(
        summary = "Фильтрация обращений с пагинацией",
        description =
            "Поддерживает фильтры: status, channel, direction, priority, organizationId, " +
                "assignedOperatorId, assignmentGroupId, skillGroupId, createdById, subject, contactEmail, " +
                "createdFrom, createdTo. Сортировка через sortBy + sortDirection.",
    )
    fun filter(
        @ModelAttribute filter: AppealFilterRequest,
    ): Page<AppealResponse> = appealService.filter(filter)

    @GetMapping("/{id}")
    @Operation(summary = "Получить обращение по ID")
    fun getById(
        @PathVariable id: UUID,
    ): AppealResponse = appealService.getById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать обращение")
    fun create(
        @Valid @RequestBody request: AppealRequest,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): AppealResponse = appealService.create(request, principal)

    @PutMapping("/{id}")
    @Operation(summary = "Обновить обращение (редактируемые поля)")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AppealUpdateRequest,
    ): AppealResponse = appealService.update(id, request)

    @PostMapping("/{id}/take")
    @Operation(
        summary = "Взять обращение в работу",
        description = "Назначает текущего оператора и переводит статус в IN_PROGRESS.",
    )
    fun takeIntoWork(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): AppealResponse = appealService.takeIntoWork(id, principal)

    @PostMapping("/{id}/assign")
    @Operation(
        summary = "Назначить обращение",
        description =
            "Назначить можно одним из трёх вариантов (взаимоисключающих): " +
                "**operatorId** — прямое назначение на оператора; " +
                "**assignmentGroupId** — на группу назначения; " +
                "**skillGroupId** — на скилл-группу. " +
                "При групповом назначении операторы подключаются через POST /operators/join.",
    )
    fun assign(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AssignOperatorRequest,
    ): AppealResponse = appealService.assign(id, request)

    @PostMapping("/{id}/operators/join")
    @Operation(
        summary = "Присоединиться к работе над обращением",
        description =
            "Добавляет текущего оператора в список activeOperators. " +
                "Доступно только для обращений, назначенных на группу. " +
                "Статус переходит в IN_PROGRESS при первом присоединении.",
    )
    fun joinWork(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): AppealResponse = appealService.joinWork(id, principal)

    @DeleteMapping("/{id}/operators/leave")
    @Operation(
        summary = "Покинуть обращение",
        description = "Убирает текущего оператора из activeOperators. Статус обращения не меняется.",
    )
    fun leaveWork(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): AppealResponse = appealService.leaveWork(id, principal)

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Изменить статус обращения",
        description =
            "Допустимые переходы: " +
                "PENDING_PROCESSING → IN_PROGRESS | SPAM | CLOSED; " +
                "IN_PROGRESS → WAITING_CLIENT_RESPONSE | CLOSED | SPAM; " +
                "WAITING_CLIENT_RESPONSE → IN_PROGRESS | CLOSED | SPAM; " +
                "SPAM → CLOSED | IN_PROGRESS.",
    )
    fun changeStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ChangeStatusRequest,
    ): AppealResponse = appealService.changeStatus(id, request.status)

    @PostMapping("/{id}/spam")
    @Operation(summary = "Пометить обращение как спам")
    fun markAsSpam(
        @PathVariable id: UUID,
    ): AppealResponse = appealService.markAsSpam(id)

    @PostMapping("/{id}/close")
    @Operation(summary = "Закрыть обращение")
    fun close(
        @PathVariable id: UUID,
    ): AppealResponse = appealService.close(id)

    @GetMapping("/{id}/messages")
    @Operation(summary = "История переписки по обращению (хронологически)")
    fun getMessages(
        @PathVariable id: UUID,
        @ParameterObject @PageableDefault(size = 50) pageable: Pageable,
    ): Page<AppealMessageResponse> = appealService.getMessages(id, pageable)

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Отправить сообщение клиенту",
        description = "Оператор отправляет сообщение. Обращение переходит в WAITING_CLIENT_RESPONSE.",
    )
    fun sendOperatorMessage(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AppealMessageRequest,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): AppealMessageResponse = appealService.sendOperatorMessage(id, request, principal)

    @PostMapping("/{id}/messages/inbound")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Зарегистрировать входящее сообщение от клиента",
        description = "Используется webhook-обработчиками. Обращение переходит в IN_PROGRESS.",
    )
    fun receiveClientMessage(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AppealMessageRequest,
    ): AppealMessageResponse = appealService.receiveClientMessage(id, request)

    @GetMapping("/{id}/events")
    @Operation(
        summary = "История событий по обращению",
        description =
            "Возвращает хронологический журнал всех событий: создание, смены статуса, назначения операторов/групп, " +
                "отправка и получение сообщений. Поддерживает пагинацию.",
    )
    fun getEvents(
        @PathVariable id: UUID,
        @ParameterObject @PageableDefault(size = 50) pageable: Pageable,
    ): Page<AppealEventResponse> = appealService.getEvents(id, pageable)

    @GetMapping("/{id}/actions")
    @Operation(
        summary = "Доступные действия по обращению для текущего пользователя",
        description =
            "Возвращает список действий, которые текущий оператор может выполнить над обращением, " +
                "а также список допустимых переходов из текущего статуса.",
    )
    fun getActions(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal,
    ): AppealActionsResponse = appealService.getActions(id, principal)

    @PostMapping("/fetch")
    @Operation(
        summary = "Пакетная загрузка обращений по списку ID",
        description = "Максимум 200 ID за запрос. Порядок ответа не гарантируется.",
    )
    fun fetchByIds(
        @Valid @RequestBody request: FetchByIdsRequest,
    ): List<AppealResponse> = appealService.fetchByIds(request.ids)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить обращение (только администратор)")
    fun delete(
        @PathVariable id: UUID,
    ) = appealService.delete(id)
}
