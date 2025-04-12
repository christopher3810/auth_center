package com.auth.api.rest.exception.handler

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.ErrorResponseFactory
import com.auth.exception.AlreadyUserExistsException
import com.auth.exception.UserNotFoundException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.util.NoSuchElementException

/**
 * 사용자 관련 예외를 처리하는 핸들러
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class UserExceptionHandler {
    /**
     * 사용자 조회 예외 처리
     */
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(
        ex: UserNotFoundException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> =
        ErrorResponseFactory.createErrorResponse(
            status = ErrorConstants.NOT_FOUND_STATUS,
            exception = ex,
            request = request,
            userMessage = ErrorConstants.USER_NOT_FOUND_MESSAGE,
            logMessage = ErrorConstants.USER_NOT_FOUND_LOG,
        )

    /**
     * 이미 존재하는 사용자 예외 처리
     */
    @ExceptionHandler(AlreadyUserExistsException::class)
    fun handleAlreadyUserExists(
        ex: AlreadyUserExistsException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> =
        ErrorResponseFactory.createErrorResponse(
            status = ErrorConstants.CONFLICT_STATUS,
            exception = ex,
            request = request,
            userMessage = ErrorConstants.USER_ALREADY_EXISTS_MESSAGE,
            logMessage = ErrorConstants.USER_ALREADY_EXISTS_LOG,
        )

    /**
     * NoSuchElementException 처리
     */
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> =
        ErrorResponseFactory.createErrorResponse(
            status = ErrorConstants.NOT_FOUND_STATUS,
            exception = ex,
            request = request,
            userMessage = ErrorConstants.ELEMENT_NOT_FOUND_MESSAGE,
            logMessage = ErrorConstants.ELEMENT_NOT_FOUND_LOG,
        )
}
