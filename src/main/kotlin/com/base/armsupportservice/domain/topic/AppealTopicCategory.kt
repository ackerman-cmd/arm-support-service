package com.base.armsupportservice.domain.topic

enum class AppealTopicCategory(
    val label: String,
) {
    ACCOUNT_AND_CARD("Счёт и карта"),
    DIGITAL_BANKING("Цифровые сервисы"),
    PAYMENTS_AND_TRANSFERS("Платежи и переводы"),
    LOANS_AND_CREDITS("Кредиты и займы"),
    SECURITY("Безопасность"),
    TECHNICAL_ISSUES("Технические проблемы"),
    GENERAL("Общие вопросы"),
}
