package com.auth.domain.auth.model

import java.time.Instant

/**
 * 액세스 토큰 도메인 모델
 * 액세스 토큰은 RefreshToken과 달리 DB에 저장되지 않고 메모리상에서만 존재.
 */
class AccessToken(
    val tokenValue: String,
    val userId: Long,
    val subject: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val roles: Set<String> = setOf(),
    val permissions: Set<String> = setOf()
) {
    /**
     * 토큰이 만료되었는지 확인
     */
    fun isExpired(): Boolean {
        return Instant.now().isAfter(expiresAt)
    }
    
    /**
     * 토큰의 남은 유효 시간(초)
     */
    fun remainingValidityInSeconds(): Long {
        if (isExpired()) return 0
        return expiresAt.epochSecond - Instant.now().epochSecond
    }
    
    /**
     * 특정 역할을 가지는지 확인
     */
    fun hasRole(role: String): Boolean {
        return roles.contains(role)
    }
    
    /**
     * 특정 권한을 가지는지 확인
     */
    fun hasPermission(permission: String): Boolean {
        return permissions.contains(permission)
    }
} 