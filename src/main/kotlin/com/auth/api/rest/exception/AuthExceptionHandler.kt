package com.auth.api.rest.exception

import com.auth.exception.InvalidAuthorizationHeaderException
import com.auth.exception.InvalidCredentialsException
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExtractionException
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
 * 인증/인가 관련 예외를 처리하는 핸들러
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
class AuthExceptionHandler {

    /**
     * Authorization 헤더 관련 예외 처리
     */
    @ExceptionHandler(InvalidAuthorizationHeaderException::class)
    fun handleInvalidAuthorizationHeaderException(ex: InvalidAuthorizationHeaderException, request: WebRequest): ResponseEntity<ApiErrorResponse> {
        logger.warn(ex) { "인증 실패 - 유효하지 않은 Authorization 헤더" }
        
        val errorResponse = ApiErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = HttpStatus.UNAUTHORIZED.reasonPhrase,
            message = ex.message ?: "유효하지 않은 Authorization 헤더입니다.",
            path = request.getDescription(false).replace("uri=", "")
        )
        
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    /**
     * 인증 정보(아이디/비밀번호)가 유효하지 않을 때 발생하는 예외 처리
     */
    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException, request: WebRequest): ResponseEntity<ApiErrorResponse> {
        logger.warn(ex) { "인증 실패 - 유효하지 않은 인증 정보" }
        
        val errorResponse = ApiErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = HttpStatus.UNAUTHORIZED.reasonPhrase,
            message = ex.message ?: "아이디 또는 비밀번호가 맞지 않습니다.",
            path = request.getDescription(false).replace("uri=", "")
        )
        
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    /**
     * 토큰 유효성 검증 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(InvalidTokenException::class)
    fun handleInvalidTokenException(ex: InvalidTokenException, request: WebRequest): ResponseEntity<ApiErrorResponse> {
        logger.warn(ex) { "유효하지 않은 토큰" }
        
        val errorResponse = ApiErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = HttpStatus.UNAUTHORIZED.reasonPhrase,
            message = ex.message ?: "유효하지 않은 토큰입니다.",
            path = request.getDescription(false).replace("uri=", "")
        )
        
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    /**
     * 토큰에서 정보 추출 실패 시 발생하는 예외 처리
     */
    @ExceptionHandler(TokenExtractionException::class)
    fun handleTokenExtractionException(ex: TokenExtractionException, request: WebRequest): ResponseEntity<ApiErrorResponse> {
        logger.error(ex) { "토큰 파싱/추출 중 오류" }
        
        val errorResponse = ApiErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = HttpStatus.UNAUTHORIZED.reasonPhrase,
            message = "토큰에서 사용자 정보를 추출할 수 없습니다.",
            path = request.getDescription(false).replace("uri=", "")
        )
        
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }
} 