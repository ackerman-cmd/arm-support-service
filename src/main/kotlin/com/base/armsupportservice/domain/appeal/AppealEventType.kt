package com.base.armsupportservice.domain.appeal

enum class AppealEventType(
    val label: String,
) {
    CREATED("Обращение создано"),
    STATUS_CHANGED("Статус изменён"),
    ASSIGNED_OPERATOR("Назначен оператор"),
    ASSIGNED_GROUP("Назначена группа"),
    OPERATOR_JOINED("Оператор присоединился"),
    OPERATOR_LEFT("Оператор покинул обращение"),
    MESSAGE_SENT("Оператор отправил сообщение"),
    MESSAGE_RECEIVED("Получено сообщение от клиента"),
    UPDATED("Обращение обновлено"),
    DELETED("Обращение удалено"),
}
