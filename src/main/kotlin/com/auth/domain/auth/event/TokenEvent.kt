package com.auth.domain.auth.event

import com.auth.domain.auth.model.RefreshToken
import java.time.LocalDateTime

/**
 * 토큰 도메인 이벤트 추상 클래스
 */
sealed class TokenEvent(
    open val token: RefreshToken,
    open val timestamp: LocalDateTime = LocalDateTime.now(),
)

/**
 * 토큰 생성 이벤트
 */
data class TokenCreatedEvent(
    override val token: RefreshToken,
) : TokenEvent(token)

/**
 * 토큰 사용 이벤트
 */
data class TokenUsedEvent(
    override val token: RefreshToken,
) : TokenEvent(token)

/**
 * 토큰 차단 이벤트
 */
data class TokenRevokedEvent(
    override val token: RefreshToken,
) : TokenEvent(token)

/**
 * 토큰 만료 이벤트
 */
data class TokenExpiredEvent(
    override val token: RefreshToken,
) : TokenEvent(token)
