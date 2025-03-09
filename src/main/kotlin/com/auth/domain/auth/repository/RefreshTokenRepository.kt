package com.auth.domain.auth.repository

import com.auth.domain.auth.model.RefreshToken
import java.time.LocalDateTime
import java.util.Optional

/**
 * 리프레시 토큰 도메인 리포지토리 인터페이스
 * 
 * DDD 원칙에 따라 도메인 계층에 위치하며, 도메인 객체의 영속성 추상화를 담당합니다.
 * 구현체는 인프라스트럭처 계층에 존재합니다.
 */
interface RefreshTokenRepository {

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
    fun findByUserIdAndValidTrue(userId: Long, currentTime: LocalDateTime): List<RefreshToken>

    /**
     * 특정 사용자의 모든 토큰 차단 처리
     */
    fun revokeAllUserTokens(userId: Long): Int

    /**
     * 만료된 토큰 삭제
     */
    fun deleteAllExpiredTokens(now: LocalDateTime): Int
    
    /**
     * 토큰 저장
     */
    fun save(refreshToken: RefreshToken): RefreshToken
    
    /**
     * 토큰 삭제
     */
    fun delete(refreshToken: RefreshToken)
    
    /**
     * ID로 토큰 찾기
     */
    fun findById(id: Long): Optional<RefreshToken>
} 