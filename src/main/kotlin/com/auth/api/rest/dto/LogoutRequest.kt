package com.auth.api.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

/**
 * 로그아웃 요청 DTO
 * 로그아웃 시 무효화할 토큰 정보를 담습니다.
 */
data class LogoutRequest(
    @Schema(description = "리프레시 토큰 값", example = "long-refresh-token-value-here")
    @field:NotBlank(message = "리프레시 토큰은 필수입니다.")
    val refreshToken: String,
)
