package com.auth.api.rest.exception.handler

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.ErrorResponseFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

/**
 * 유효성 검증 관련 예외를 처리하는 핸들러
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ValidationExceptionHandler {
    /**
     * MethodArgumentNotValidException 예외 처리
     * Request Body의 유효성 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> {
        // 바인딩 결과에서 필드 오류 추출
        val fieldErrors = extractFieldErrors(ex)

        // 유효성 검증 오류 응답 생성
        return ErrorResponseFactory.createValidationErrorResponse(
            status = ErrorConstants.BAD_REQUEST_STATUS,
            message = ErrorConstants.VALIDATION_ERROR_MESSAGE,
            fieldErrors = fieldErrors,
            request = request,
        )
    }

    /**
     * 바인딩 결과에서 필드 오류 추출
     */
    private fun extractFieldErrors(ex: MethodArgumentNotValidException): List<ErrorDetail.FieldErrorDetail> =
        ex.bindingResult.allErrors.mapNotNull { error ->
            when (error) {
                is FieldError ->
                    ErrorDetail.FieldErrorDetail(
                        field = error.field,
                        message = error.defaultMessage ?: ErrorConstants.FIELD_ERROR_DEFAULT_MESSAGE,
                    )
                else -> null
            }
        }
}
