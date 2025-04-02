package com.auth.api.rest.dto

/**
 * 토큰 검증 응답 DTO
 * 토큰 검증 결과와 사용자 정보를 담습니다.
 */
data class TokenValidationResponse(
    val valid: Boolean,
    val username: String? = null,
    val authorities: List<String> = emptyList(),
    val error: String? = null,
)
