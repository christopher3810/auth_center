package com.auth.`interface`.rest.dto

/**
 * 로그인 요청 DTO
 * 사용자 인증을 위한 요청 데이터를 담습니다.
 */
data class LoginRequest(
    val username: String,
    val password: String
) 