package com.auth.api.rest.exception.handler

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.ErrorResponseFactory
import com.auth.exception.InvalidAuthorizationHeaderException
import com.auth.exception.InvalidCredentialsException
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExtractionException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

/**
 * 인증/인가 관련 예외를 처리하는 핸들러
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
class AuthExceptionHandler {
    /**
     * Authorization 헤더 관련 예외 처리
     */
    @ExceptionHandler(InvalidAuthorizationHeaderException::class)
    fun handleInvalidAuthorizationHeaderException(
        ex: InvalidAuthorizationHeaderException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> =
        ErrorResponseFactory.createErrorResponse(
            status = ErrorConstants.UNAUTHORIZED_STATUS,
            ex = ex,
            request = request,
            defaultMessage = ErrorConstants.INVALID_AUTH_HEADER_MESSAGE,
            logMessage = ErrorConstants.AUTH_HEADER_ERROR_LOG,
        )

    /**
     * 인증 정보(아이디/비밀번호)가 유효하지 않을 때 발생하는 예외 처리
     */
    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(
        ex: InvalidCredentialsException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> =
        ErrorResponseFactory.createErrorResponse(
            status = ErrorConstants.UNAUTHORIZED_STATUS,
            ex = ex,
            request = request,
            defaultMessage = ErrorConstants.INVALID_CREDENTIALS_MESSAGE,
            logMessage = ErrorConstants.INVALID_CREDENTIALS_LOG,
        )

    /**
     * 유효하지 않은 토큰 예외 처리
     */
    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(
        ex: InvalidTokenException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> =
        ErrorResponseFactory.createErrorResponse(
            status = ErrorConstants.UNAUTHORIZED_STATUS,
            ex = ex,
            request = request,
            defaultMessage = ErrorConstants.INVALID_TOKEN_MESSAGE,
            logMessage = ErrorConstants.INVALID_TOKEN_LOG,
        )

    /**
     * 토큰 추출 예외 처리
     */
    @ExceptionHandler(TokenExtractionException::class)
    fun handleTokenExtractionException(
        ex: TokenExtractionException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> =
        ErrorResponseFactory.createErrorResponse(
            status = ErrorConstants.UNAUTHORIZED_STATUS,
            ex = ex,
            request = request,
            defaultMessage = ErrorConstants.TOKEN_EXTRACTION_ERROR_MESSAGE,
            logMessage = ErrorConstants.TOKEN_PARSING_ERROR_LOG,
        )
}
