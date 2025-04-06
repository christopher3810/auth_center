package com.auth.api.rest.exception.handler

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.ErrorExamples
import com.auth.api.rest.exception.ErrorResponseFactory
import com.auth.exception.AlreadyUserExistsException
import com.auth.exception.UserNotFoundException
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
    @ApiResponse(
        responseCode = "404",
        description = "사용자를 찾을 수 없음",
        content = [
            Content(
                mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
                schema = Schema(implementation = ErrorDetail::class),
                examples = [
                    ExampleObject(
                        name = "user-not-found",
                        summary = "존재하지 않는 사용자",
                        value = ErrorExamples.USER_NOT_FOUND_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    fun handleUserNotFoundException(
        ex: UserNotFoundException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> =
        ErrorResponseFactory.createErrorResponse(
            status = ErrorConstants.NOT_FOUND_STATUS,
            ex = ex,
            request = request,
            defaultMessage = ErrorConstants.USER_NOT_FOUND_MESSAGE,
            logMessage = ErrorConstants.USER_NOT_FOUND_LOG,
        )

    /**
     * 이미 존재하는 사용자 예외 처리
     */
    @ExceptionHandler(AlreadyUserExistsException::class)
    @ApiResponse(
        responseCode = "409",
        description = "이미 존재하는 사용자",
        content = [
            Content(
                mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
                schema = Schema(implementation = ErrorDetail::class),
                examples = [
                    ExampleObject(
                        name = "user-already-exists",
                        summary = "이미 존재하는 사용자",
                        value = ErrorExamples.USER_ALREADY_EXISTS_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    fun handleAlreadyUserExists(
        ex: AlreadyUserExistsException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> =
        ErrorResponseFactory.createErrorResponse(
            status = ErrorConstants.CONFLICT_STATUS,
            ex = ex,
            request = request,
            defaultMessage = ErrorConstants.USER_ALREADY_EXISTS_MESSAGE,
            logMessage = ErrorConstants.USER_ALREADY_EXISTS_LOG,
        )

    /**
     * NoSuchElementException 처리
     */
    @ExceptionHandler(NoSuchElementException::class)
    @ApiResponse(
        responseCode = "404",
        description = "요청한 요소를 찾을 수 없음",
        content = [
            Content(
                mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
                schema = Schema(implementation = ErrorDetail::class),
                examples = [
                    ExampleObject(
                        name = "element-not-found",
                        summary = "요소를 찾을 수 없음",
                        value = ErrorExamples.NOT_FOUND_ERROR_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    fun handleNoSuchElementException(
        ex: NoSuchElementException,
        request: WebRequest,
    ): ResponseEntity<ErrorDetail> =
        ErrorResponseFactory.createErrorResponse(
            status = ErrorConstants.NOT_FOUND_STATUS,
            ex = ex,
            request = request,
            defaultMessage = ErrorConstants.ELEMENT_NOT_FOUND_MESSAGE,
            logMessage = ErrorConstants.ELEMENT_NOT_FOUND_LOG,
        )
}
