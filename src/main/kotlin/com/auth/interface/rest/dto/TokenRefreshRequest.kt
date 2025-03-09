package com.auth.`interface`.rest.dto

/**
 * 토큰 갱신 요청 DTO
 * 리프레시 토큰을 이용하여 새 액세스 토큰을 요청하기 위한 데이터를 담습니다.
 */
data class TokenRefreshRequest(
    val refreshToken: String
) 