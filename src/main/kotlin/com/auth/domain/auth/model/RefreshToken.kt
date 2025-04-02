package com.auth.domain.auth.model

import com.auth.domain.auth.event.TokenEvent
import com.auth.domain.auth.event.TokenRevokedEvent
import com.auth.domain.auth.event.TokenUsedEvent
import java.time.LocalDateTime

/**
 * 리프레시 토큰 도메인 모델
 */
class RefreshToken(
    val id: Long = 0,
    val token: String,
    val userId: Long,
    val userEmail: String,
    val expiryDate: LocalDateTime,
    var used: Boolean = false,
    var revoked: Boolean = false,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    // 도메인 이벤트를 저장하는 리스트
    private val domainEvents = mutableListOf<TokenEvent>()

    /**
     * 토큰이 만료되었는지 확인
     */
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiryDate)

    /**
     * 토큰이 유효한지 확인 (만료되지 않고, 사용되지 않았으며, 차단되지 않음)
     */
    fun isValid(): Boolean = !isExpired() && !used && !revoked

    /**
     * 토큰 사용 처리
     */
    fun markAsUsed(): RefreshToken {
        if (!used) {
            used = true
            registerEvent(TokenUsedEvent(this))
        }
        return this
    }

    /**
     * 토큰 차단 처리
     */
    fun revoke(): RefreshToken {
        if (!revoked) {
            revoked = true
            registerEvent(TokenRevokedEvent(this))
        }
        return this
    }

    /**
     * 이벤트 등록
     */
    fun registerEvent(event: TokenEvent) {
        domainEvents.add(event)
    }

    /**
     * 도메인 이벤트 조회 및 소비
     */
    fun consumeEvents(): List<TokenEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }
}
