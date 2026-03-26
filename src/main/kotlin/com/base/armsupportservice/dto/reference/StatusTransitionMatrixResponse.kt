package com.base.armsupportservice.dto.reference

import com.base.armsupportservice.domain.appeal.AppealStatus

data class StatusTransitionMatrixResponse(
    /** Для каждого статуса — список допустимых целевых статусов */
    val matrix: Map<AppealStatus, List<AppealStatus>>,
    /** Описания статусов с метками и флагом терминальности */
    val statuses: Map<AppealStatus, StatusMeta>,
) {
    data class StatusMeta(
        val label: String,
        val description: String,
        val terminal: Boolean,
    )
}
