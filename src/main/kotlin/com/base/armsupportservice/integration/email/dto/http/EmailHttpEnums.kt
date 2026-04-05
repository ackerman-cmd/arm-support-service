package com.base.armsupportservice.integration.email.dto.http

import com.fasterxml.jackson.annotation.JsonValue

/** Соответствует перечислению MessageStatus из контракта HTTP API email-integration. */
enum class MessageStatus(
    @JsonValue val wire: String,
) {
    RECEIVED_META("RECEIVED_META"),
    CONTENT_FETCHING("CONTENT_FETCHING"),
    RECEIVED_FULL("RECEIVED_FULL"),
    THREAD_MATCHED("THREAD_MATCHED"),
    CASE_LINKED("CASE_LINKED"),
    PROCESSING_FAILED("PROCESSING_FAILED"),
    PENDING_SEND("PENDING_SEND"),
    SENDING("SENDING"),
    SENT("SENT"),
    DELIVERED("DELIVERED"),
    FAILED("FAILED"),
    BOUNCED("BOUNCED"),
    CANCELED("CANCELED"),
}

enum class ConversationStatus(
    @JsonValue val wire: String,
) {
    OPEN("OPEN"),
    CLOSED("CLOSED"),
    ARCHIVED("ARCHIVED"),
}

enum class MessageDirection(
    @JsonValue val wire: String,
) {
    INBOUND("INBOUND"),
    OUTBOUND("OUTBOUND"),
}

enum class RecipientType(
    @JsonValue val wire: String,
) {
    TO("TO"),
    CC("CC"),
    BCC("BCC"),
}
