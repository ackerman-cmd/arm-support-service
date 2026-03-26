package com.base.armsupportservice.dto.appeal

import jakarta.validation.constraints.AssertTrue
import java.util.UUID

/**
 * Запрос на назначение обращения.
 *
 * Варианты использования (взаимоисключающие):
 * - **Прямое назначение на оператора**: передать только [operatorId].
 *   Оператор становится единственным ответственным и сразу попадает в activeOperators.
 * - **Назначение на группу назначения**: передать только [assignmentGroupId].
 *   Операторы группы могут затем присоединиться через POST /operators/join.
 * - **Назначение на скилл-группу**: передать только [skillGroupId].
 *   Аналогично — групповая работа.
 *
 * Поле [clearActiveOperators] (по умолчанию true) сбрасывает список активных операторов
 * при переназначении. Установите false, если нужно оставить текущих работающих.
 */
data class AssignOperatorRequest(
    val operatorId: UUID? = null,
    val assignmentGroupId: UUID? = null,
    val skillGroupId: UUID? = null,
    val clearActiveOperators: Boolean = true,
) {
    @AssertTrue(message = "Необходимо указать ровно одно из: operatorId, assignmentGroupId, skillGroupId")
    fun isExactlyOneTarget(): Boolean {
        val count = listOfNotNull(operatorId, assignmentGroupId, skillGroupId).size
        return count == 1
    }
}
