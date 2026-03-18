package com.base.armsupportservice.domain.appeal

enum class AppealStatus {
    /** Входящее обращение — ожидает взятия в работу */
    PENDING_PROCESSING,

    /** Оператор взял обращение в работу */
    IN_PROGRESS,

    /** Оператор ответил — ожидаем ответа клиента */
    WAITING_CLIENT_RESPONSE,

    /** Обращение закрыто */
    CLOSED,

    /** Помечено как спам */
    SPAM,
}
