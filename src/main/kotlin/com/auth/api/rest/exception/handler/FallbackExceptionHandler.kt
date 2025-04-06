package com.auth.api.rest.exception.handler

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.ErrorExamples
import com.auth.api.rest.exception.ErrorResponseFactory
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

/**
 * 다른 핸들러에서 처리되지 않은 모든 예외를 처리하는 최후의 예외 처리기
 */
@RestControllerAdvice
@Order(Int.MAX_VALUE)
class FallbackExceptionHandler {
    private val logger = LoggerFactory.getLogger(FallbackExceptionHandler::class.java)

    /**
     * 처리되지 않은 모든 예외를 처리하는 핸들러
     */
    @ExceptionHandler(Exception::class)
    @ApiResponse(
        responseCode = "500",
        description = "서버 내부 오류",
        content = [
            Content(
                mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
                schema = Schema(implementation = ErrorDetail::class),
                examples = [
                    ExampleObject(
                        name = "server-error",
                        summary = "예상치 못한 서버 내부 오류",
                        value = ErrorExamples.SERVER_ERROR_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    fun handleAllUncaughtException(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> {
        logger.error("Unhandled exception encountered", ex)
        return ErrorResponseFactory.createServerErrorResponse(
            ex = ex,
            request = request,
            logMessage = ErrorConstants.INTERNAL_SERVER_ERROR_LOG,
        )
    }
}
