package com.auth.infrastructure.repository

import com.auth.domain.auth.model.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

/**
 * 리프레시 토큰 정보에 접근하기 위한 리포지토리
 */
@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

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
    fun findByUserIdAndUsedFalseAndRevokedFalseAndExpiryDateAfter(
        userId: Long, 
        currentTime: LocalDateTime
    ): List<RefreshToken>

    /**
     * 특정 사용자의 모든 토큰 차단 처리
     */
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.userId = :userId")
    fun revokeAllUserTokens(userId: Long): Int

    /**
     * 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    fun deleteAllExpiredTokens(now: LocalDateTime): Int
} 