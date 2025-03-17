package com.auth.application.auth.service

import com.auth.domain.auth.service.RefreshTokenDomainService
import com.auth.domain.auth.service.TokenValidator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

private val logger = KotlinLogging.logger {}

/**
 * 토큰 블랙리스트 관리 애플리케이션 서비스
 * 애플리케이션 계층에 위치하여 도메인 로직을 조정하고 외부 서비스와 연동합니다.
 */
@Service
class TokenBlacklistService(
    private val tokenValidator: TokenValidator,
    private val refreshTokenDomainService: RefreshTokenDomainService
) {
    companion object {
        private const val TYPE_CLAIM = "type"
        private const val USER_ID_CLAIM = "userId"
        private const val REFRESH_TYPE = "refresh"
    }

    /**
     * 토큰을 블랙리스트에 추가합니다.
     *
     * @param token JWT 토큰
     * @return 처리 결과
     */
    @Transactional
    fun addToBlacklist(token: String): Boolean = runCatching {
        // 토큰 유효성 검증
        if (!tokenValidator.validateToken(token)) {
            logger.warn { "블랙리스트 추가 실패: 유효하지 않은 토큰" }
            return false
        }

        // 토큰에서 사용자 정보 추출
        tokenValidator.getClaims(token).let { claims ->
            val userId = extractUserId(claims) ?: return false
            
            when (claims[TYPE_CLAIM]?.toString()) {
                REFRESH_TYPE -> handleRefreshTokenBlacklisting(token)
                else -> handleAccessTokenBlacklisting(userId, token)
            }
        }
    }.getOrElse { e ->
        logger.error(e) { "토큰 블랙리스트 추가 중 오류 발생: ${token.trim()}" }
        false
    }

    /**
     * 클레임에서 사용자 ID를 추출합니다.
     */
    private fun extractUserId(claims: Map<String, Any?>): Long? {
        val userId = claims[USER_ID_CLAIM]?.toString()?.toLongOrNull()
        if (userId == null) {
            logger.warn { "블랙리스트 추가 실패: 토큰에 사용자 ID가 없음" }
        }
        return userId
    }

    /**
     * 리프레시 토큰을 블랙리스트에 추가합니다.
     */
    private fun handleRefreshTokenBlacklisting(token: String): Boolean {
        val revokedToken = refreshTokenDomainService.revokeToken(token)
        val success = processRevokeResult(revokedToken)
        logRevokeResult(success, token)
        return success
    }

    /**
     * 리프레시 토큰 취소 결과를 처리합니다.
     */
    private fun processRevokeResult(revokedToken: Optional<*>): Boolean {
        return revokedToken.isPresent
    }

    /**
     * 리프레시 토큰 취소 결과를 로깅합니다.
     */
    private fun logRevokeResult(success: Boolean, token: String) {
        if (success) {
            logger.debug { "리프레시 토큰 블랙리스트 추가 성공: ${token.trim()}" }
        } else {
            logger.warn { "리프레시 토큰 블랙리스트 처리 실패: ${token.trim()}" }
        }
    }

    /**
     * 액세스 토큰 무효화를 위해 해당 사용자의 모든 리프레시 토큰을 차단합니다.
     */
    private fun handleAccessTokenBlacklisting(userId: Long, token: String): Boolean {
        val revokedCount = refreshTokenDomainService.revokeAllUserTokens(userId)
        logger.debug { "액세스 토큰 블랙리스트 추가로 인해 ${revokedCount}개의 리프레시 토큰 차단됨 (토큰: ${token.trim()})" }
        return true
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     *
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    fun isBlacklisted(token: String): Boolean = runCatching {
        extractTokenTypeAndCheckBlacklist(token)
    }.getOrElse { e ->
        logger.error(e) { "토큰 블랙리스트 확인 중 오류 발생" }
        false
    }

    /**
     * 토큰의 타입을 추출하고 블랙리스트 여부를 확인합니다.
     */
    private fun extractTokenTypeAndCheckBlacklist(token: String): Boolean {
        val claims = tokenValidator.getClaims(token)
        val tokenType = claims[TYPE_CLAIM]?.toString()
        
        return when (tokenType) {
            REFRESH_TYPE -> isRefreshTokenBlacklisted(token)
            else -> false // 액세스 토큰은 여기서는 별도의 블랙리스트 상태를 관리하지 않음
        }
    }

    /**
     * 리프레시 토큰이 블랙리스트에 있는지 확인합니다.
     */
    private fun isRefreshTokenBlacklisted(token: String): Boolean {
        val refreshToken = refreshTokenDomainService.findByToken(token)
        return refreshToken.map { !it.isValid() }.orElse(false)
    }

    /**
     * 만료된 토큰을 도메인 서비스(RefreshTokenService)를 통해 정리합니다.
     */
    @Transactional
    fun cleanupExpiredTokens() {
        val deletedCount = refreshTokenDomainService.removeExpiredTokens()
        logCleanupResult(deletedCount)
    }
    
    /**
     * 토큰 정리 결과를 로깅합니다.
     */
    private fun logCleanupResult(deletedCount: Int) {
        logger.info { "만료된 토큰 $deletedCount 개 삭제됨" }
    }
} 