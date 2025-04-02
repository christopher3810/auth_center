package com.auth.domain.auth.entity

import com.auth.infrastructure.audit.Traceable
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
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
     * 토큰 사용 여부
     */
    @Column(nullable = false)
    var used: Boolean = false,
    /**
     * 토큰 차단 여부
     */
    @Column(nullable = false)
    var revoked: Boolean = false,
    @Embedded
    val traceable: Traceable = Traceable(),
)
