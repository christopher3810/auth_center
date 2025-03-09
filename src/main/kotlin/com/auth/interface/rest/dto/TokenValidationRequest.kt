package com.auth.`interface`.rest.dto

/**
 * 토큰 검증 요청 DTO
 * 토큰의 유효성을 검증하기 위한 요청 데이터를 담습니다.
 */
data class TokenValidationRequest(
    val token: String
) 