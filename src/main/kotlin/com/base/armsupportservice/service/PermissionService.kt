package com.base.armsupportservice.service

import com.base.armsupportservice.domain.appeal.Appeal
import com.base.armsupportservice.domain.appeal.AppealAction
import com.base.armsupportservice.domain.appeal.AppealStatus
import com.base.armsupportservice.dto.appeal.AppealActionsResponse.ActionItem
import com.base.armsupportservice.security.UserPrincipal
import org.springframework.stereotype.Service

@Service
class PermissionService {
    /**
     * Оценивает **все** возможные действия над обращением для данного пользователя.
     * Каждое действие получает флаг [ActionItem.enabled] и подсказку [ActionItem.hint]:
     * - enabled=true  → hint объясняет, что произойдёт при нажатии
     * - enabled=false → hint объясняет, почему действие недоступно
     */
    fun evaluateActions(
        appeal: Appeal,
        principal: UserPrincipal,
    ): List<ActionItem> {
        val hasRead = principal.hasPermission(APPEAL_READ)
        val hasWrite = principal.hasPermission(APPEAL_WRITE)
        val hasAdmin = principal.hasPermission(ADMIN_ACCESS)

        val isTerminal = appeal.status in TERMINAL_STATUSES
        val isClosed = appeal.status == AppealStatus.CLOSED
        val isSpam = appeal.status == AppealStatus.SPAM
        val hasGroupAssignment = appeal.assignmentGroupId != null || appeal.skillGroupId != null
        val isAlreadyActive = principal.userId in appeal.activeOperatorIds

        return AppealAction.entries.map { action ->
            eval(action) {
                when (action) {
                    AppealAction.VIEW_MESSAGES -> {
                        if (hasRead || hasWrite || hasAdmin) {
                            enable("История переписки по обращению доступна для просмотра")
                        } else {
                            disable("Для просмотра переписки необходимо право $APPEAL_READ")
                        }
                    }

                    AppealAction.TAKE_INTO_WORK -> {
                        when {
                            !hasWrite && !hasAdmin ->
                                disable("Для взятия в работу необходимо право $APPEAL_WRITE")

                            appeal.status == AppealStatus.IN_PROGRESS ->
                                disable("Обращение уже находится в работе")

                            isTerminal ->
                                disable("Действие недоступно для закрытых обращений")

                            appeal.status !in TAKEABLE_STATUSES ->
                                disable("Взять в работу можно только из статусов «Ожидает обработки» или «Ожидает ответа клиента»")

                            else ->
                                enable("Вы станете ответственным оператором, статус перейдёт в «В работе»")
                        }
                    }

                    AppealAction.ASSIGN_OPERATOR -> {
                        when {
                            !hasWrite && !hasAdmin ->
                                disable("Для назначения необходимо право $APPEAL_WRITE")

                            isTerminal ->
                                disable("Переназначение недоступно для закрытых обращений")

                            else ->
                                enable(
                                    "Назначьте на конкретного оператора, группу назначения или скилл-группу. " +
                                        "При групповом назначении операторы подключаются через «Присоединиться»",
                                )
                        }
                    }

                    AppealAction.REPLY_TO_CLIENT -> {
                        when {
                            !hasWrite && !hasAdmin ->
                                disable("Для ответа клиенту необходимо право $APPEAL_WRITE")

                            appeal.status == AppealStatus.PENDING_PROCESSING ->
                                disable("Сначала возьмите обращение в работу, затем можно отвечать клиенту")

                            isTerminal ->
                                disable("Нельзя ответить по закрытому обращению")

                            appeal.status !in REPLYABLE_STATUSES ->
                                disable("Ответ доступен только когда обращение «В работе» или «Ожидает ответа клиента»")

                            else ->
                                enable("После отправки сообщения статус перейдёт в «Ожидает ответа клиента»")
                        }
                    }

                    AppealAction.CHANGE_STATUS -> {
                        when {
                            !hasWrite && !hasAdmin ->
                                disable("Для смены статуса необходимо право $APPEAL_WRITE")

                            isClosed ->
                                disable("Статус закрытого обращения изменить нельзя")

                            else ->
                                enable(
                                    "Вручную переведите обращение в другой статус. " +
                                        "Допустимые переходы определяются матрицей статусов",
                                )
                        }
                    }

                    AppealAction.MARK_AS_SPAM -> {
                        when {
                            !hasWrite && !hasAdmin ->
                                disable("Для пометки спама необходимо право $APPEAL_WRITE")

                            isSpam ->
                                disable("Обращение уже помечено как спам")

                            isClosed ->
                                disable("Нельзя пометить как спам закрытое обращение")

                            else ->
                                enable("Обращение будет помечено как нежелательное и скрыто из основной очереди")
                        }
                    }

                    AppealAction.CLOSE -> {
                        when {
                            !hasWrite && !hasAdmin ->
                                disable("Для закрытия необходимо право $APPEAL_WRITE")

                            isClosed ->
                                disable("Обращение уже закрыто")

                            else ->
                                enable("Работа по обращению будет завершена — статус изменится на «Закрыто»")
                        }
                    }

                    AppealAction.EDIT -> {
                        when {
                            !hasWrite && !hasAdmin ->
                                disable("Для редактирования необходимо право $APPEAL_WRITE")

                            isTerminal ->
                                disable("Редактирование закрытых обращений недоступно")

                            else ->
                                enable("Измените тему, описание, тематику, приоритет или контактные данные")
                        }
                    }

                    AppealAction.JOIN_WORK -> {
                        when {
                            !hasWrite && !hasAdmin ->
                                disable("Для присоединения необходимо право $APPEAL_WRITE")

                            !hasGroupAssignment ->
                                disable(
                                    "Присоединиться можно только к обращениям, назначенным на группу. " +
                                        "При прямом назначении на оператора групповая работа недоступна",
                                )

                            isAlreadyActive ->
                                disable("Вы уже являетесь активным участником по данному обращению")

                            isTerminal ->
                                disable("Нельзя присоединиться к закрытому обращению")

                            else ->
                                enable(
                                    "Вы будете добавлены в список активных операторов. " +
                                        "Если статус «Ожидает обработки» или «Ожидает ответа клиента» — он перейдёт в «В работе»",
                                )
                        }
                    }

                    AppealAction.LEAVE_WORK -> {
                        when {
                            !hasWrite && !hasAdmin ->
                                disable("Для выхода из обращения необходимо право $APPEAL_WRITE")

                            !isAlreadyActive ->
                                disable("Вы не являетесь активным участником по данному обращению")

                            else ->
                                enable("Вы будете убраны из списка активных операторов. Статус обращения не изменится")
                        }
                    }

                    AppealAction.DELETE -> {
                        if (hasAdmin) {
                            enable("Обращение и вся переписка будут безвозвратно удалены")
                        } else {
                            disable("Удаление доступно только пользователям с правом $ADMIN_ACCESS")
                        }
                    }
                }
            }
        }
    }

    /**
     * Возвращает только множество **доступных** действий.
     * Используется в бизнес-логике сервисов и тестах.
     */
    fun availableActions(
        appeal: Appeal,
        principal: UserPrincipal,
    ): Set<AppealAction> = evaluateActions(appeal, principal).filter { it.enabled }.map { it.action }.toSet()

    // -------- DSL helpers --------

    private fun eval(
        action: AppealAction,
        block: EvalScope.() -> Unit,
    ): ActionItem {
        val scope = EvalScope(action)
        scope.block()
        return scope.build()
    }

    private class EvalScope(
        private val action: AppealAction,
    ) {
        private var enabled: Boolean = false
        private var hint: String = ""

        fun enable(hint: String) {
            this.enabled = true
            this.hint = hint
        }

        fun disable(hint: String) {
            this.enabled = false
            this.hint = hint
        }

        fun build() =
            ActionItem(
                action = action,
                label = action.label,
                description = action.description,
                enabled = enabled,
                hint = hint,
            )
    }

    private fun UserPrincipal.hasPermission(permission: String): Boolean = permission in permissions

    companion object {
        const val APPEAL_READ = "APPEAL_READ"
        const val APPEAL_WRITE = "APPEAL_WRITE"
        const val ADMIN_ACCESS = "ADMIN_ACCESS"

        val TERMINAL_STATUSES = setOf(AppealStatus.CLOSED, AppealStatus.SPAM)
        val TAKEABLE_STATUSES = setOf(AppealStatus.PENDING_PROCESSING, AppealStatus.WAITING_CLIENT_RESPONSE)
        val REPLYABLE_STATUSES = setOf(AppealStatus.IN_PROGRESS, AppealStatus.WAITING_CLIENT_RESPONSE)
    }
}
