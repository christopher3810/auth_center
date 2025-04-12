package com.auth.api.rest.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.WebRequest

/**
 * 오류 응답 생성을 위한 유틸리티 객체
 * 모든 예외 핸들러에서 공통으로 사용하는 응답 생성 기능 제공
 */
object ErrorResponseFactory {
    private val logger = KotlinLogging.logger {}

    private val log = KotlinLogging.logger {}

    private fun extractPath(request: WebRequest): String = request.getDescription(false).substringAfter("uri=")

    fun createErrorResponse(
        status: HttpStatus,
        exception: Exception,
        request: WebRequest,
        userMessage: String,
        logMessage: String = ErrorConstants.DEFAULT_ERROR_LOG,
    ): ResponseEntity<ErrorDetail> {
        if (status.is5xxServerError) {
            log.error(exception) { logMessage }
        } else {
            log.warn(exception) { logMessage }
        }

        val path = extractPath(request)
        val errorDetail = ErrorDetail.of(status, userMessage, path)

        return ResponseEntity.status(status).body(errorDetail)
    }

    fun createValidationErrorResponse(
        status: HttpStatus,
        message: String,
        fieldErrors: List<ErrorDetail.FieldErrorDetail>,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> {
        log.warn { ErrorConstants.VALIDATION_ERROR_LOG }

        val path = extractPath(request)
        val errorDetail = ErrorDetail.validation(status, message, fieldErrors, path)

        return ResponseEntity.status(status).body(errorDetail)
    }

    fun createServerErrorResponse(
        exception: Exception,
        request: WebRequest,
        logMessage: String = ErrorConstants.SERVER_ERROR_LOG,
    ): ResponseEntity<ErrorDetail> {
        log.error(exception) { logMessage }

        val path = extractPath(request)
        val errorDetail =
            ErrorDetail.of(
                status = ErrorConstants.SERVER_ERROR_STATUS,
                detail = ErrorConstants.SERVER_ERROR_MESSAGE,
                path = path,
            )

        return ResponseEntity
            .status(ErrorConstants.SERVER_ERROR_STATUS)
            .body(errorDetail)
    }
}
