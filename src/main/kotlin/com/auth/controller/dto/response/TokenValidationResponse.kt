package com.auth.controller.dto.response

/**
 * 토큰 검증 응답 DTO
 */
data class TokenValidationResponse(
    val valid: Boolean,
    val username: String? = null,
    val userId: String? = null
) 