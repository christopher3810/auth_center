package com.auth.api.security.annotation.api

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.ErrorExamples
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.Retention
import kotlin.annotation.Target

/**
 * 계정 활성화 API에 필요한 권한 제어와 문서화를 통합한 어노테이션
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
@Operation(
    summary = "계정 활성화",
    description = """
        지정된 사용자 계정을 활성화 상태로 변경합니다.
        
        ## 요구 권한
        - 관리자(ADMIN) 역할이 필요합니다.
        
        ## 처리 과정
        1. 사용자 ID로 계정 조회
        2. 계정 상태를 활성(ACTIVE)으로 변경
        3. 변경 성공 여부 반환
    """,
)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "200",
            description = "활성화 성공",
        ),
        ApiResponse(
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
        ),
        ApiResponse(
            responseCode = "404",
            description = "해당 사용자가 존재하지 않음",
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
        ),
    ],
)
annotation class AdminAccountActivationApi
