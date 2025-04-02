package com.auth.api.rest.exception

import java.time.LocalDateTime

/**
 * 표준 에러 응답 객체
 */
data class ApiErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)

/**
 * 유효성 검증 실패 시 응답 객체
 */
data class ValidationErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val fieldErrors: Map<String, String>,
    val path: String,
)
