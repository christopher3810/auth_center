package com.auth.`interface`.rest.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * 입력 데이터 유효성 검증 관련 예외를 처리하는 핸들러
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
class ValidationExceptionHandler {

    /**
     * 요청 파라미터 유효성 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<ValidationErrorResponse> {
        logger.warn(ex) { "유효성 검증 실패" }
        
        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = if (error is FieldError) error.field else error.objectName
            val message = error.defaultMessage ?: "유효하지 않은 값입니다."
            fieldName to message
        }
        
        val errorResponse = ValidationErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "입력값 검증에 실패했습니다.",
            fieldErrors = errors,
            path = request.getDescription(false).replace("uri=", "")
        )
        
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

} 