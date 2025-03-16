package com.auth.domain.auth.service

import com.auth.domain.auth.entity.RefreshTokenEntity
import com.auth.domain.auth.factory.RefreshTokenFactory
import com.auth.domain.auth.model.RefreshToken
import com.auth.domain.auth.repository.RefreshTokenRepository
import com.auth.infrastructure.config.JwtConfig
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Optional

/**
 * 리프레시 토큰 도메인 서비스
 * 이렇게 조회와 생성 책임을 명확히 분리합니다.
 */
@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenGenerator: TokenGenerator,
    private val jwtConfig: JwtConfig
) {

    /**
     * 토큰 값으로 리프레시 토큰 조회
     */
    @Transactional(readOnly = true)
    fun findByToken(token: String): Optional<RefreshToken> {
        // 1. Entity 조회
        val tokenEntityOpt = refreshTokenRepository.findByToken(token)
        
        // 2. Entity -> Model 변환 (Factory 사용)
        return tokenEntityOpt.map { RefreshTokenFactory.createFromEntity(it) }
    }
    
    /**
     * 사용자 ID로 리프레시 토큰 목록 조회
     */
    @Transactional(readOnly = true)
    fun findByUserId(userId: Long): List<RefreshToken> {
        // 1. Entity 목록 조회
        val tokenEntities = refreshTokenRepository.findByUserId(userId)
        
        // 2. Entity -> Model 변환 (Factory 사용)
        return tokenEntities.map { RefreshTokenFactory.createFromEntity(it) }
    }
    
    /**
     * 사용자 ID로 유효한 리프레시 토큰 목록 조회
     */
    @Transactional(readOnly = true)
    fun findValidTokensByUserId(userId: Long): List<RefreshToken> {
        // 1. Entity 목록 조회
        val currentTime = LocalDateTime.now()
        val tokenEntities = refreshTokenRepository.findByUserIdAndValidTrue(userId, currentTime)
        
        // 2. Entity -> Model 변환 (Factory 사용)
        return tokenEntities.map { RefreshTokenFactory.createFromEntity(it) }
    }
    
    /**
     * 사용자를 위한 새 리프레시 토큰 생성
     * 
     * 이 메서드는 도메인 서비스로서 다음과 같은 책임을 가집니다:
     * 1. TokenGenerator를 통해 토큰 문자열 생성 (인프라 계층에 위임)
     * 2. Factory를 통해 도메인 모델 생성 (도메인 계층 내부)
     * 3. Repository를 통해 엔티티 저장 (영속성 계층에 위임)
     */
    @Transactional
    fun generateRefreshToken(subject: String, userId: Long): RefreshToken {
        // 1. 인프라 계층에 토큰 문자열 생성 요청
        val tokenValue = tokenGenerator.generateRefreshTokenString(subject, userId)
        
        // 2. 도메인 모델 생성 (Factory 사용)
        val refreshTokenModel = RefreshTokenFactory.createToken(
            token = tokenValue,
            userId = userId,
            userEmail = subject,
            expiryTimeInMinutes = getRefreshTokenExpiryInMinutes()
        )
        
        // 3. 도메인 모델 저장
        return saveToken(refreshTokenModel)
    }
    
    /**
     * 리프레시 토큰 만료 시간 계산 (분 단위)
     * JwtConfig에서 설정값을 가져와 사용
     */
    private fun getRefreshTokenExpiryInMinutes(): Long {
        // 초 단위를 분 단위로 변환 (60초 = 1분)
        return jwtConfig.refreshTokenValidityInSeconds / 60
    }
    
    /**
     * 새 리프레시 토큰 생성 (토큰 값을 직접 제공하는 경우)
     */
    @Transactional
    fun createToken(
        token: String,
        userId: Long,
        userEmail: String,
        expiryTimeInMinutes: Long
    ): RefreshToken {
        // 1. Factory를 통해 도메인 모델 생성
        val refreshTokenModel = RefreshTokenFactory.createToken(
            token = token,
            userId = userId,
            userEmail = userEmail,
            expiryTimeInMinutes = expiryTimeInMinutes
        )
        
        // 2. 도메인 모델 저장
        return saveToken(refreshTokenModel)
    }
    
    /**
     * 토큰 사용 처리
     */
    @Transactional
    fun markTokenAsUsed(tokenValue: String): Optional<RefreshToken> {
        return findByToken(tokenValue).map { token ->
            // 도메인 모델에 비즈니스 로직 적용
            token.markAsUsed()
            
            // 도메인 모델 저장
            saveToken(token)
        }
    }
    
    /**
     * 토큰 차단 처리
     */
    @Transactional
    fun revokeToken(tokenValue: String): Optional<RefreshToken> {
        return findByToken(tokenValue).map { token ->
            // 도메인 모델에 비즈니스 로직 적용
            token.revoke()
            
            // 도메인 모델 저장
            saveToken(token)
        }
    }
    
    /**
     * 특정 사용자의 모든 토큰 차단
     */
    @Transactional
    fun revokeAllUserTokens(userId: Long): Int {
        return refreshTokenRepository.revokeAllUserTokens(userId)
    }
    
    /**
     * 만료된 토큰 삭제
     */
    @Transactional
    fun removeExpiredTokens(): Int {
        return refreshTokenRepository.deleteAllExpiredTokens(LocalDateTime.now())
    }
    
    /**
     * 리프레시 토큰 모델 저장
     */
    @Transactional
    fun saveToken(refreshToken: RefreshToken): RefreshToken {
        // 1. Entity 생성 또는 업데이트
        val tokenEntity: RefreshTokenEntity = if (refreshToken.id == 0L) {
            // 신규 토큰인 경우
            RefreshTokenFactory.createEntity(refreshToken)
        } else {
            // 기존 토큰 업데이트인 경우
            val existingEntity = refreshTokenRepository.findById(refreshToken.id)
                .orElseThrow { IllegalArgumentException("존재하지 않는 토큰 ID: ${refreshToken.id}") }
            
            // Entity 업데이트
            RefreshTokenFactory.updateEntity(existingEntity, refreshToken)
        }
        
        // 2. Entity 저장
        val savedEntity = refreshTokenRepository.save(tokenEntity)
        
        // 3. 저장된 Entity -> 도메인 모델 변환 (Factory 사용)
        return RefreshTokenFactory.createFromEntity(savedEntity)
    }
    
    /**
     * 리프레시 토큰 삭제
     */
    @Transactional
    fun deleteToken(refreshToken: RefreshToken) {
        // 1. ID로 엔티티 조회
        refreshTokenRepository.findById(refreshToken.id).ifPresent { entity ->
            // 2. 엔티티 삭제
            refreshTokenRepository.delete(entity)
        }
    }
} 