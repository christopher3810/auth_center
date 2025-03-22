package com.auth.`interface`.rest.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * 다른 예외 핸들러에서 처리되지 않은 모든 예외를 처리하는 폴백 핸들러
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class FallbackExceptionHandler {

    /**
     * 다른 핸들러에서 처리되지 않은 모든 예외를 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtExceptions(ex: Exception, request: WebRequest): ResponseEntity<ApiErrorResponse> {
        logger.error(ex) { "처리되지 않은 예외 발생" }
        
        val errorResponse = ApiErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = "서버 내부 오류가 발생했습니다.",
            path = request.getDescription(false).replace("uri=", "")
        )
        
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
} 