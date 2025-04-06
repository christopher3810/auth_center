package com.auth.application.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

/**
 * 토큰 발급 결과를 담는 DTO
 *
 * @property accessToken 액세스 토큰
 * @property refreshToken 리프레시 토큰
 * @property tokenType 토큰 타입 (Bearer)
 * @property expiresIn 액세스 토큰 만료 시간(분)
 * @property refreshTokenExpiresIn 리프레시 토큰 만료 시간(분), 리프레시 토큰 발급 시에만 값이 있음
 * @property refreshTokenIssuedAt 리프레시 토큰 발급 시간, 새 리프레시 토큰 발급 시에만 값이 있음
 * @property isNewRefreshToken 새로 발급된 리프레시 토큰인지 여부
 */
@Schema(
    description = "인증 토큰 응답 객체",
    title = "TokenDto",
    requiredProperties = ["accessToken", "refreshToken", "tokenType", "expiresIn"],
)
data class TokenDto(
    @Schema(
        description = "액세스 토큰 - API 요청 시 인증에 사용",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9....",
        format = "jwt",
    )
    val accessToken: String,
    @Schema(
        description = "리프레시 토큰 - 액세스 토큰 갱신에 사용",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        format = "jwt",
    )
    val refreshToken: String,
    @Schema(
        description = "토큰 타입",
        example = "Bearer",
        allowableValues = ["Bearer"],
    )
    val tokenType: String = TOKEN_TYPE_BEARER,
    @Schema(
        description = "액세스 토큰 만료 시간(초)",
        example = "3600",
        minimum = "0",
    )
    val expiresIn: Long,
    @Schema(
        description = "리프레시 토큰 만료 시간(분)",
        example = "10080",
        minimum = "0",
        nullable = true,
    )
    val refreshTokenExpiresIn: Long? = null,
    @Schema(
        description = "리프레시 토큰 발급 시간",
        format = "date-time",
        nullable = true,
    )
    val refreshTokenIssuedAt: Instant? = null,
    @Schema(
        description = "새로 발급된 리프레시 토큰인지 여부",
        example = "true",
    )
    val isNewRefreshToken: Boolean = false,
) {
    companion object {
        /**
         * Bearer 토큰 타입 상수
         */
        const val TOKEN_TYPE_BEARER = "Bearer"
    }
}
