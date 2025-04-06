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

    /**
     * 기본 오류 응답 생성
     * 일반적인 오류 응답 생성을 위한 주요 메서드
     */
    fun createErrorResponse(
        status: HttpStatus,
        ex: Exception,
        request: WebRequest,
        defaultMessage: String,
        logMessage: String = ErrorConstants.DEFAULT_ERROR_LOG,
    ): ResponseEntity<ErrorDetail> {
        // 로깅 - 4xx는 warn, 5xx는 error 수준
        when {
            status.is5xxServerError -> logger.error(ex) { logMessage }
            else -> logger.warn(ex) { logMessage }
        }

        // 경로 추출 및 오류 상세 정보 생성
        val path = request.extractPath()
        val errorDetail = ErrorDetail.forStatus(status, defaultMessage, path)

        return ResponseEntity.status(status).body(errorDetail)
    }

    /**
     * 유효성 검증 오류 응답 생성
     */
    fun createValidationErrorResponse(
        status: HttpStatus,
        message: String,
        fieldErrors: List<ErrorDetail.FieldErrorDetail>,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> {
        // 유효성 검증 오류는 항상 warn 레벨로 로깅
        logger.warn { ErrorConstants.VALIDATION_ERROR_LOG }

        // 경로 추출 및 필드 오류 세부 정보 포함
        val path = request.extractPath()
        val errorDetail = ErrorDetail.forValidationError(status, message, fieldErrors, path)

        return ResponseEntity.status(status).body(errorDetail)
    }

    /**
     * 서버 오류 응답 생성
     */
    fun createServerErrorResponse(
        ex: Exception,
        request: WebRequest,
        logMessage: String = ErrorConstants.SERVER_ERROR_LOG,
    ): ResponseEntity<ErrorDetail> {
        // 서버 오류는 항상 error 레벨로 로깅
        logger.error(ex) { logMessage }

        // 경로 추출 및 서버 오류 정보 생성
        val path = request.extractPath()
        val errorDetail =
            ErrorDetail.forStatus(
                status = ErrorConstants.SERVER_ERROR_STATUS,
                detail = ErrorConstants.SERVER_ERROR_MESSAGE,
                path = path,
            )

        return ResponseEntity.status(ErrorConstants.SERVER_ERROR_STATUS).body(errorDetail)
    }
}

/**
 * WebRequest에서 요청 경로를 추출하는 확장 함수
 */
fun WebRequest.extractPath(): String = this.getDescription(false).substringAfter("uri=")
