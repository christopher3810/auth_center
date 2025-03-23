package com.auth.api.rest.dto

import com.auth.application.auth.dto.TokenDto

/**
 * 토큰 응답 DTO
 * 인증 성공 시 클라이언트에게 반환되는 토큰 정보를 담습니다.
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
) {
    companion object {
        // 정적 팩토리 메서드
        fun from(dto: TokenDto): TokenResponse {
            return TokenResponse(
                accessToken = dto.accessToken,
                refreshToken = dto.refreshToken,
                expiresIn = dto.expiresIn
            )
        }
    }
}