package com.auth.api.rest.exception.handler

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.ErrorExamples
import com.auth.api.rest.exception.ErrorResponseFactory
import com.auth.exception.InvalidAuthorizationHeaderException
import com.auth.exception.InvalidCredentialsException
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExtractionException
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
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
    @ApiResponse(
        responseCode = "401",
        description = "인증 헤더 오류",
        content = [
            Content(
                mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
                schema = Schema(implementation = ErrorDetail::class),
                examples = [
                    ExampleObject(
                        name = "invalid-auth-header",
                        summary = "잘못된 인증 헤더 형식",
                        value = ErrorExamples.INVALID_AUTH_HEADER_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
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
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = [
            Content(
                mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
                schema = Schema(implementation = ErrorDetail::class),
                examples = [
                    ExampleObject(
                        name = "invalid-credentials",
                        summary = "잘못된 사용자명 또는 비밀번호",
                        value = ErrorExamples.INVALID_CREDENTIALS_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
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
    @ApiResponse(
        responseCode = "401",
        description = "유효하지 않은 토큰",
        content = [
            Content(
                mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
                schema = Schema(implementation = ErrorDetail::class),
                examples = [
                    ExampleObject(
                        name = "invalid-token",
                        summary = "만료되거나 조작된 토큰",
                        value = ErrorExamples.INVALID_TOKEN_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
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
    @ApiResponse(
        responseCode = "401",
        description = "토큰에서 정보 추출 실패",
        content = [
            Content(
                mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
                schema = Schema(implementation = ErrorDetail::class),
                examples = [
                    ExampleObject(
                        name = "token-extraction-error",
                        summary = "토큰에서 필수 정보 추출 실패",
                        value = ErrorExamples.INVALID_TOKEN_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
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
