package com.auth.infrastructure.repository

import com.auth.domain.auth.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenEntity, Long> {

    /**
     * 토큰 값으로 리프레시 토큰 찾기
     */
    fun findByToken(token: String): Optional<RefreshTokenEntity>

    /**
     * 사용자 ID로 리프레시 토큰 찾기
     */
    fun findByUserId(userId: Long): List<RefreshTokenEntity>

    /**
     * 사용자 ID로 유효한 리프레시 토큰 찾기
     */
    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.userId = :userId AND r.used = false AND r.revoked = false AND r.expiryDate > :currentTime")
    fun findValidTokensByUserId(
        @Param("userId") userId: Long,
        @Param("currentTime") currentTime: LocalDateTime
    ): List<RefreshTokenEntity>

    /**
     * 특정 사용자의 모든 토큰 차단 처리
     */
    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userId = :userId")
    fun revokeAllUserTokens(@Param("userId") userId: Long): Int

    /**
     * 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiryDate < :now")
    fun deleteAllExpiredTokens(@Param("now") now: LocalDateTime): Int
} 