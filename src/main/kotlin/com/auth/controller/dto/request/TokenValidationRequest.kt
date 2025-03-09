package com.auth.controller.dto.request

import jakarta.validation.constraints.NotBlank

/**
 * 토큰 검증 요청 DTO
 */
data class TokenValidationRequest(
    @field:NotBlank(message = "Token cannot be blank")
    val token: String
) 