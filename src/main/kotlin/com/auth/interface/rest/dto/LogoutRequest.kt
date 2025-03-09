package com.auth.`interface`.rest.dto

/**
 * 로그아웃 요청 DTO
 * 로그아웃 시 무효화할 토큰 정보를 담습니다.
 */
data class LogoutRequest(
    val token: String
) 