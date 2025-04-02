package com.auth.domain.auth.service

import com.auth.domain.auth.factory.RefreshTokenFactory
import com.auth.domain.auth.model.RefreshToken
import com.auth.domain.auth.repository.RefreshTokenRepository
import com.auth.infrastructure.config.JwtConfig
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 리프레시 토큰 도메인 서비스
 */
@Service
class RefreshTokenDomainService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenGenerator: TokenGenerator,
    private val jwtConfig: JwtConfig,
) {
    /**
     * 토큰 값으로 리프레시 토큰 조회
     */
    @Transactional(readOnly = true)
    fun findByToken(token: String): RefreshToken? = refreshTokenRepository.findByToken(token)?.let(RefreshTokenFactory::createFromEntity)

    /**
     * 사용자 ID로 리프레시 토큰 목록 조회
     */
    @Transactional(readOnly = true)
    fun findByUserId(userId: Long): List<RefreshToken> =
        refreshTokenRepository
            .findByUserId(userId)
            .map(RefreshTokenFactory::createFromEntity)

    /**
     * 사용자 ID로 유효한 리프레시 토큰 목록 조회
     */
    @Transactional(readOnly = true)
    fun findValidTokensByUserId(userId: Long): List<RefreshToken> =
        refreshTokenRepository
            .findByUserIdAndValidTrue(userId, LocalDateTime.now())
            .map(RefreshTokenFactory::createFromEntity)

    /**
     * 사용자를 위한 새 리프레시 토큰 생성
     */
    @Transactional
    fun generateRefreshToken(
        subject: String,
        userId: Long,
    ): RefreshToken {
        val tokenValue = tokenGenerator.generateRefreshTokenString(subject, userId)

        return RefreshTokenFactory
            .createToken(
                token = tokenValue,
                userId = userId,
                userEmail = subject,
                expiryTimeInMinutes = getRefreshTokenExpiryInMinutes(),
            ).also { saveToken(it) }
    }

    /**
     * 리프레시 토큰 만료 시간 계산 (분 단위)
     */
    private fun getRefreshTokenExpiryInMinutes(): Long = jwtConfig.refreshTokenValidityInSeconds / 60 // 초 단위를 분 단위로 변환

    /**
     * 새 리프레시 토큰 생성 (토큰 값을 직접 제공하는 경우)
     */
    @Transactional
    fun createToken(
        token: String,
        userId: Long,
        userEmail: String,
        expiryTimeInMinutes: Long,
    ): RefreshToken =
        RefreshTokenFactory
            .createToken(
                token = token,
                userId = userId,
                userEmail = userEmail,
                expiryTimeInMinutes = expiryTimeInMinutes,
            ).also { saveToken(it) }

    /**
     * 토큰 사용 처리
     */
    @Transactional
    fun markTokenAsUsed(tokenValue: String): RefreshToken? =
        findByToken(tokenValue)?.also { token ->
            token.markAsUsed()
            saveToken(token)
        }

    /**
     * 토큰 차단 처리
     */
    @Transactional
    fun revokeToken(tokenValue: String): RefreshToken? =
        findByToken(tokenValue)?.also { token ->
            token.revoke()
            saveToken(token)
        }

    /**
     * 특정 사용자의 모든 토큰 차단
     */
    @Transactional
    fun revokeAllUserTokens(userId: Long): Int = refreshTokenRepository.revokeAllUserTokens(userId)

    /**
     * 만료된 토큰 삭제
     */
    @Transactional
    fun removeExpiredTokens(): Int = refreshTokenRepository.deleteAllExpiredTokens(LocalDateTime.now())

    /**
     * 리프레시 토큰 모델 저장
     */
    @Transactional
    fun saveToken(refreshToken: RefreshToken): RefreshToken {
        val tokenEntity =
            when (refreshToken.id) {
                0L -> {
                    // 신규 토큰인 경우
                    RefreshTokenFactory.createEntity(refreshToken)
                }
                else -> {
                    val existingEntity =
                        refreshTokenRepository.findById(refreshToken.id)
                            ?: throw IllegalArgumentException("존재하지 않는 토큰 ID: ${refreshToken.id}")

                    RefreshTokenFactory.updateEntity(existingEntity, refreshToken)
                }
            }

        return refreshTokenRepository
            .save(tokenEntity)
            .let(RefreshTokenFactory::createFromEntity)
    }

    /**
     * 리프레시 토큰 삭제
     */
    @Transactional
    fun deleteToken(refreshToken: RefreshToken) {
        refreshTokenRepository.findById(refreshToken.id)?.let { entity ->
            refreshTokenRepository.delete(entity)
        }
    }
}
