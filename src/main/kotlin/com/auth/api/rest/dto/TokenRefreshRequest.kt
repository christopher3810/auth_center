package com.auth.api.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

/**
 * 토큰 갱신 요청 DTO
 * 리프레시 토큰을 이용하여 새 액세스 토큰을 요청하기 위한 데이터를 담습니다.
 */
data class TokenRefreshRequest(
    @Schema(description = "리프레시 토큰 값", example = "long-refresh-token-value-here")
    @field:NotBlank(message = "리프레시 토큰은 필수입니다.")
    val refreshToken: String,
)
