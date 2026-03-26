package com.base.armsupportservice.dto.appeal

import com.base.armsupportservice.domain.appeal.AppealAction
import com.base.armsupportservice.domain.appeal.AppealStatus
import java.util.UUID

/**
 * Ответ на запрос доступных действий по обращению.
 *
 * Возвращает **все** возможные действия с флагом [ActionItem.enabled] и подсказкой [ActionItem.hint].
 * Фронт использует это, чтобы:
 * - отображать кнопки действий всегда (не только доступные)
 * - грей-аутить недоступные действия
 * - показывать [hint] в tooltip/описании: при enabled=true — что произойдёт, при enabled=false — почему недоступно
 */
data class AppealActionsResponse(
    val appealId: UUID,
    val currentStatus: AppealStatus,
    val currentStatusLabel: String,
    /** Полный список действий с флагом доступности и объяснением */
    val actions: List<ActionItem>,
    /** Список статусов, в которые можно перейти из текущего (для дропдауна смены статуса) */
    val availableStatusTransitions: List<StatusTransitionItem>,
) {
    data class ActionItem(
        val action: AppealAction,
        val label: String,
        /** Постоянное описание что делает действие */
        val description: String,
        /** true — действие разрешено для текущего пользователя и состояния обращения */
        val enabled: Boolean,
        /**
         * Подсказка для фронта (tooltip / sub-label):
         * - enabled=true  → краткое описание что произойдёт при выполнении
         * - enabled=false → конкретная причина, почему действие недоступно
         */
        val hint: String,
    )

    data class StatusTransitionItem(
        val status: AppealStatus,
        val label: String,
    )
}
