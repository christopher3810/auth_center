package com.auth.domain.auth.entity

import com.auth.infrastructure.audit.Traceable
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 리프레시 토큰 엔티티 (데이터베이스 매핑용)
 * 모델링 문서 참고: "Refresh 토큰처럼 영속되어야 하는 경우 도메인 객체로 간주할 수 있습니다."
 *
 * 주: DDD 원칙에 따라 엔티티는 데이터베이스 매핑에 집중하고,
 * 도메인 객체 생성 및 변환 책임은 Factory로 이동했습니다.
 */
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
    val traceable: Traceable = Traceable()
)