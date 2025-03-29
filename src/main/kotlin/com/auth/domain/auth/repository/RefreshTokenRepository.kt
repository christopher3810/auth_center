package com.auth.domain.auth.repository

import com.auth.domain.auth.entity.RefreshTokenEntity
import java.time.LocalDateTime
import java.util.Optional

/**
 * 리프레시 토큰 도메인 리포지토리 인터페이스
 */
interface RefreshTokenRepository {

    /**
     * ID로 토큰 엔티티 찾기
     */
    fun findById(id: Long): RefreshTokenEntity?

    /**
     * 토큰 값으로 리프레시 토큰 엔티티 찾기
     */
    fun findByToken(token: String): RefreshTokenEntity?

    /**
     * 사용자 ID로 리프레시 토큰 엔티티 찾기
     */
    fun findByUserId(userId: Long): List<RefreshTokenEntity>

    /**
     * 사용자 ID로 유효한 리프레시 토큰 엔티티 찾기
     */
    fun findByUserIdAndValidTrue(userId: Long, currentTime: LocalDateTime): List<RefreshTokenEntity>

    /**
     * 특정 사용자의 모든 토큰 차단 처리
     */
    fun revokeAllUserTokens(userId: Long): Int

    /**
     * 만료된 토큰 삭제
     */
    fun deleteAllExpiredTokens(now: LocalDateTime): Int
    
    /**
     * 토큰 엔티티 저장
     */
    fun save(refreshTokenEntity: RefreshTokenEntity): RefreshTokenEntity
    
    /**
     * 토큰 엔티티 삭제
     */
    fun delete(refreshTokenEntity: RefreshTokenEntity)
} 