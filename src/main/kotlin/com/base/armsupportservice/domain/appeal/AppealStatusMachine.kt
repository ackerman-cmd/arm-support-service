package com.base.armsupportservice.domain.appeal

import com.base.armsupportservice.exception.InvalidStatusTransitionException

/**
 * Описывает допустимые переходы между статусами обращения.
 *
 * Входящее (INBOUND):
 *   PENDING_PROCESSING → IN_PROGRESS (взятие в работу)
 *   IN_PROGRESS → WAITING_CLIENT_RESPONSE (оператор ответил)
 *   WAITING_CLIENT_RESPONSE → IN_PROGRESS (клиент ответил / повторное взятие)
 *   IN_PROGRESS | WAITING_CLIENT_RESPONSE → CLOSED
 *   любой активный → SPAM
 *
 * Исходящее (OUTBOUND):
 *   Создаётся сразу в WAITING_CLIENT_RESPONSE
 *   Далее те же переходы, что у INBOUND начиная с WAITING_CLIENT_RESPONSE
 */
object AppealStatusMachine {
    private val allowedTransitions: Map<AppealStatus, Set<AppealStatus>> =
        mapOf(
            AppealStatus.PENDING_PROCESSING to
                setOf(
                    AppealStatus.IN_PROGRESS,
                    AppealStatus.SPAM,
                    AppealStatus.CLOSED,
                ),
            AppealStatus.IN_PROGRESS to
                setOf(
                    AppealStatus.WAITING_CLIENT_RESPONSE,
                    AppealStatus.CLOSED,
                    AppealStatus.SPAM,
                ),
            AppealStatus.WAITING_CLIENT_RESPONSE to
                setOf(
                    AppealStatus.IN_PROGRESS,
                    AppealStatus.CLOSED,
                    AppealStatus.SPAM,
                ),
            AppealStatus.SPAM to
                setOf(
                    AppealStatus.CLOSED,
                    AppealStatus.IN_PROGRESS,
                ),
            AppealStatus.CLOSED to emptySet(),
        )

    fun validate(
        from: AppealStatus,
        to: AppealStatus,
    ) {
        val allowed = allowedTransitions[from] ?: emptySet()
        if (to !in allowed) {
            throw InvalidStatusTransitionException(from, to)
        }
    }

    fun initialStatus(direction: AppealDirection): AppealStatus =
        when (direction) {
            AppealDirection.INBOUND -> AppealStatus.PENDING_PROCESSING
            AppealDirection.OUTBOUND -> AppealStatus.WAITING_CLIENT_RESPONSE
        }

    /** Статус после того, как оператор отправил сообщение клиенту */
    fun afterOperatorReply(current: AppealStatus): AppealStatus {
        validate(current, AppealStatus.WAITING_CLIENT_RESPONSE)
        return AppealStatus.WAITING_CLIENT_RESPONSE
    }

    /** Статус после того, как клиент прислал ответ */
    fun afterClientReply(current: AppealStatus): AppealStatus {
        validate(current, AppealStatus.IN_PROGRESS)
        return AppealStatus.IN_PROGRESS
    }
}
