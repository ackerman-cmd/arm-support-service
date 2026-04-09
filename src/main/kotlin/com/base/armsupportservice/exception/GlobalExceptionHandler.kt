package com.base.armsupportservice.exception

import com.base.armsupportservice.dto.common.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AppealNotFoundException::class)
    fun handleAppealNotFound(ex: AppealNotFoundException): ResponseEntity<ErrorResponse> = notFound(ex.message ?: "Обращение не найдено")

    @ExceptionHandler(OrganizationNotFoundException::class)
    fun handleOrgNotFound(ex: OrganizationNotFoundException): ResponseEntity<ErrorResponse> =
        notFound(ex.message ?: "Организация не найдена")

    @ExceptionHandler(GroupNotFoundException::class)
    fun handleGroupNotFound(ex: GroupNotFoundException): ResponseEntity<ErrorResponse> = notFound(ex.message ?: "Группа не найдена")

    @ExceptionHandler(MailboxNotConfiguredException::class)
    fun handleMailboxNotConfigured(ex: MailboxNotConfiguredException): ResponseEntity<ErrorResponse> =
        notFound(ex.message ?: "Для группы не настроен почтовый ящик")

    @ExceptionHandler(OperatorNotFoundException::class)
    fun handleOperatorNotFound(ex: OperatorNotFoundException): ResponseEntity<ErrorResponse> = notFound(ex.message ?: "Оператор не найден")

    @ExceptionHandler(InvalidStatusTransitionException::class)
    fun handleInvalidTransition(ex: InvalidStatusTransitionException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(HttpStatus.CONFLICT.value(), "Conflict", ex.message ?: "Недопустимый переход"))

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicate(ex: DuplicateResourceException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(HttpStatus.CONFLICT.value(), "Conflict", ex.message ?: "Ресурс уже существует"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val details =
            ex.bindingResult.allErrors.associate { error ->
                val field = (error as? FieldError)?.field ?: "unknown"
                field to (error.defaultMessage ?: "Ошибка валидации")
            }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Validation Failed",
                    message = "Ошибки валидации входных данных",
                    details = details,
                ),
            )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(HttpStatus.FORBIDDEN.value(), "Forbidden", "Недостаточно прав для выполнения операции"))

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "Внутренняя ошибка сервера"))
    }

    private fun notFound(message: String): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", message))
}
