package com.auth.domain.auth.model
import java.time.Instant

/**
 * 토큰 정보를 저장하는 도메인 클래스
 */
data class Token(
    val id: String = "",
    val userId: String,
    val username: String,
    val tokenValue: String,
    val tokenType: TokenType,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant
)