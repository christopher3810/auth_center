package com.auth.infrastructure.repository

import com.auth.domain.auth.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

/**
 * 리프레시 토큰 정보에 접근하기 위한 JPA 리포지토리
 */
@Repository
interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 값으로 리프레시 토큰 찾기
     */
    fun findByToken(token: String): Optional<RefreshToken>

    /**
     * 사용자 ID로 리프레시 토큰 찾기
     */
    fun findByUserId(userId: Long): List<RefreshToken>

    /**
     * 사용자 ID로 유효한 리프레시 토큰 찾기
     */
    @Query("SELECT r FROM RefreshToken r WHERE r.userId = :userId AND r.used = false AND r.revoked = false AND r.expiryDate > :currentTime")
    fun findValidTokensByUserId(
        @Param("userId") userId: Long,
        @Param("currentTime") currentTime: LocalDateTime
    ): List<RefreshToken>

    /**
     * 특정 사용자의 모든 토큰 차단 처리
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.userId = :userId")
    fun revokeAllUserTokens(@Param("userId") userId: Long): Int

    /**
     * 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    fun deleteAllExpiredTokens(@Param("now") now: LocalDateTime): Int
} 