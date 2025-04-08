package com.auth.api.docs.annotations

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.ErrorExamples
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.Retention
import kotlin.annotation.Target

/**
 * 인증 실패(401) 오류 응답
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
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
                    summary = "인증 실패",
                    value = ErrorExamples.INVALID_CREDENTIALS_EXAMPLE
                )
            ]
        )
    ]
)
annotation class ApiAuthError

/**
 * 권한 부족(403) 오류 응답
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "403",
    description = "권한 부족",
    content = [
        Content(
            mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
            schema = Schema(implementation = ErrorDetail::class),
            examples = [
                ExampleObject(
                    name = "forbidden",
                    summary = "관리자 권한 필요",
                    value = ErrorExamples.FORBIDDEN_ERROR_EXAMPLE
                )
            ]
        )
    ]
)
annotation class ApiForbiddenError

/**
 * 리소스 찾기 실패(404) 오류 응답
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "요청한 자원을 찾을 수 없음",
    content = [
        Content(
            mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
            schema = Schema(implementation = ErrorDetail::class),
            examples = [
                ExampleObject(
                    name = "not-found",
                    summary = "리소스를 찾을 수 없음",
                    value = ErrorExamples.NOT_FOUND_ERROR_EXAMPLE
                )
            ]
        )
    ]
)
annotation class ApiNotFoundError

/**
 * 유효하지 않은 토큰(404) 오류 응답
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "유효하지 않은 토큰",
    content = [
        Content(
            mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
            schema = Schema(implementation = ErrorDetail::class),
            examples = [
                ExampleObject(
                    name = "invalid-token",
                    summary = "유효하지 않은 토큰",
                    value = ErrorExamples.INVALID_TOKEN_EXAMPLE
                )
            ]
        )
    ]
)
annotation class ApiInvalidTokenError

/**
 * 사용자 없음(404) 오류 응답
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
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
                    summary = "사용자 없음",
                    value = ErrorExamples.USER_NOT_FOUND_EXAMPLE
                )
            ]
        )
    ]
)
annotation class ApiUserNotFoundError

/**
 * 중복 이메일/사용자명(400) 오류 응답
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "이미 존재하는 사용자 정보",
    content = [
        Content(
            mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
            schema = Schema(implementation = ErrorDetail::class),
            examples = [
                ExampleObject(
                    name = "duplicate-email",
                    summary = "이메일 중복",
                    value = """
                    {
                      "status": 400,
                      "detail": "이미 사용 중인 이메일입니다.",
                      "type": "https://auth_center_sample_domain/errors/validation",
                      "instance": "/api/users/v1/register",
                      "timestamp": 1743941250123,
                      "traceId": "abc123",
                      "title": "Bad Request"
                    }
                    """
                )
            ]
        )
    ]
)
annotation class ApiDuplicateUserError
