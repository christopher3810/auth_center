package com.auth.api.rest.exception

import com.auth.exception.AlreadyUserExistsException
import com.auth.exception.UserNotFoundException
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
 * 사용자 도메인 관련 예외를 처리하는 핸들러
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class UserExceptionHandler {

    /**
     * 사용자를 찾을 수 없을 때 발생하는 예외 처리
     */
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException, request: WebRequest): ResponseEntity<ApiErrorResponse> {
        logger.warn(ex) { ex.message }

        val errorResponse = ApiErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = ex.message ?: "사용자를 찾을 수 없습니다.",
            path = request.getDescription(false).replace("uri=", "")
        )

        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    /**
     * 이미 존재하는 사용자일 때 발생하는 예외 처리
     */
    @ExceptionHandler(AlreadyUserExistsException::class)
    fun handleAlreadyUserExists(ex: AlreadyUserExistsException, request: WebRequest): ResponseEntity<ApiErrorResponse> {
        logger.warn(ex) { ex.message }

        val errorResponse = ApiErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = ex.message ?: "이미 존재하는 사용자입니다.",
            path = request.getDescription(false).replace("uri=", "")
        )

        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    /**
     * NoSuchElementException 예외를 사용자 관련 컨텍스트에서 처리
     * 이 핸들러는 사용자 관련 컨트롤러에서 발생한 NoSuchElementException만 처리함
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(ex: NoSuchElementException, request: WebRequest): ResponseEntity<ApiErrorResponse> {
        logger.warn(ex) { "요소를 찾을 수 없음" }

        val errorResponse = ApiErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = ex.message ?: "요청한 리소스를 찾을 수 없습니다.",
            path = request.getDescription(false).replace("uri=", "")
        )

        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }
}