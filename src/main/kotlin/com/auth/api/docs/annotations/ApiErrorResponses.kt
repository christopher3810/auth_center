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
                    value = ErrorExamples.INVALID_CREDENTIALS_EXAMPLE,
                ),
            ],
        ),
    ],
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
                    value = ErrorExamples.FORBIDDEN_ERROR_EXAMPLE,
                ),
            ],
        ),
    ],
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
                    value = ErrorExamples.NOT_FOUND_ERROR_EXAMPLE,
                ),
            ],
        ),
    ],
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
                    value = ErrorExamples.INVALID_TOKEN_EXAMPLE,
                ),
            ],
        ),
    ],
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
                    value = ErrorExamples.USER_NOT_FOUND_EXAMPLE,
                ),
            ],
        ),
    ],
)
annotation class ApiUserNotFoundError

/**
 * 중복 이메일/사용자명(409) 오류 응답
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "409",
    description = "이미 존재하는 사용자 정보",
    content = [
        Content(
            mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
            schema = Schema(implementation = ErrorDetail::class),
            examples = [
                ExampleObject(
                    name = "duplicate-user",
                    summary = "사용자 중복",
                    value = ErrorExamples.USER_ALREADY_EXISTS_EXAMPLE,
                ),
                ExampleObject(
                    name = "duplicate-email",
                    summary = "이메일 중복",
                    value = ErrorExamples.EMAIL_ALREADY_EXISTS_EXAMPLE,
                ),
            ],
        ),
    ],
)
annotation class ApiDuplicateUserError

/**
 * 잘못된 요청(400) 오류 응답
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "잘못된 요청 형식 또는 유효성 검증 실패",
    content = [
        Content(
            mediaType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
            schema = Schema(implementation = ErrorDetail::class),
            examples = [
                ExampleObject(
                    name = "validation-error",
                    summary = "요청 데이터 유효성 검증 실패",
                    value = ErrorExamples.VALIDATION_ERROR_EXAMPLE,
                ),
            ],
        ),
    ],
)
annotation class ApiBadRequestError

/**
 * 서버 오류(500) 오류 응답
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
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
annotation class ApiServerError
