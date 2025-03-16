package com.auth.application.auth.dto

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
data class TokenDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = TOKEN_TYPE_BEARER,
    val expiresIn: Long,
    val refreshTokenExpiresIn: Long? = null,
    val refreshTokenIssuedAt: Instant? = null,
    val isNewRefreshToken: Boolean = false
) {
    companion object {
        /**
         * Bearer 토큰 타입 상수
         */
        const val TOKEN_TYPE_BEARER = "Bearer"
    }
}
