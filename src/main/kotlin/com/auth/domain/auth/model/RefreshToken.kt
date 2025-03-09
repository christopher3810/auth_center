package com.auth.domain.auth.model

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 리프레시 토큰 도메인 엔티티
 * 인증 바운디드 컨텍스트의 핵심 개체로, 토큰 관련 비즈니스 로직을 캡슐화합니다.
 */
@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * 토큰 값
     */
    @Column(nullable = false, unique = true, length = 500)
    val token: String,

    /**
     * 토큰 소유자의 ID
     */
    @Column(nullable = false)
    val userId: Long,

    /**
     * 토큰 소유자의 이메일
     */
    @Column(nullable = false)
    val userEmail: String,

    /**
     * 토큰 만료 일시
     */
    @Column(nullable = false)
    val expiryDate: LocalDateTime,

    /**
     * 토큰 발급 일시
     */
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 토큰 사용 여부
     */
    @Column(nullable = false)
    var used: Boolean = false,

    /**
     * 토큰 차단 여부
     */
    @Column(nullable = false)
    var revoked: Boolean = false
) {
    /**
     * 토큰이 만료되었는지 확인
     */
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiryDate)
    }

    /**
     * 토큰이 유효한지 확인 (만료되지 않고, 사용되지 않았으며, 차단되지 않음)
     */
    fun isValid(): Boolean {
        return !isExpired() && !used && !revoked
    }

    /**
     * 토큰 사용 처리
     */
    fun markAsUsed() {
        used = true
    }

    /**
     * 토큰 차단 처리
     */
    fun revoke() {
        revoked = true
    }
} 