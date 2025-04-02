package com.auth.application.auth.service

import com.auth.domain.auth.service.RefreshTokenDomainService
import com.auth.domain.auth.service.TokenValidator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class TokenBlacklistService(
    private val tokenValidator: TokenValidator,
    private val refreshTokenDomainService: RefreshTokenDomainService,
) {
    companion object {
        private const val TYPE_CLAIM = "type"
        private const val USER_ID_CLAIM = "userId"
        private const val REFRESH_TYPE = "refresh"
        private const val TOKEN_PREVIEW_LENGTH = 10 // 토큰 로그 미리보기 길이
    }

    /**
     * 토큰을 블랙리스트에 추가합니다.
     *
     * @param token JWT 토큰
     * @return 처리 결과
     */
    @Transactional
    fun addToBlacklist(token: String): Boolean =
        runCatching {
            when {
                !tokenValidator.validateToken(token) -> {
                    logger.warn { "블랙리스트 추가 실패: 유효하지 않은 토큰" }
                    false
                }
                else -> processValidToken(token)
            }
        }.getOrElse { exception ->
            logTokenProcessingError(exception, token, "블랙리스트 추가")
        }

    /**
     * 유효한 토큰을 처리합니다.
     */
    private fun processValidToken(token: String): Boolean {
        val claims = tokenValidator.getClaims(token)
        return claims[USER_ID_CLAIM]?.toString()?.toLongOrNull()?.let { userId ->
            when (claims[TYPE_CLAIM]?.toString()) {
                REFRESH_TYPE -> handleRefreshTokenBlacklisting(token)
                else -> handleAccessTokenBlacklisting(userId, token)
            }
        } ?: run {
            logger.warn { "블랙리스트 추가 실패: 토큰에 사용자 ID가 없음" }
            false
        }
    }

    private fun handleRefreshTokenBlacklisting(token: String): Boolean =
        refreshTokenDomainService.revokeToken(token)?.let {
            logger.debug { "리프레시 토큰 블랙리스트 추가 성공: ${tokenPreview(token)}" }
            true
        } ?: run {
            logger.warn { "리프레시 토큰 블랙리스트 처리 실패: ${tokenPreview(token)}" }
            false
        }

    /**
     * 액세스 토큰 무효화를 위해 해당 사용자의 모든 리프레시 토큰을 차단.
     */
    private fun handleAccessTokenBlacklisting(
        userId: Long,
        token: String,
    ): Boolean {
        val revokedCount = refreshTokenDomainService.revokeAllUserTokens(userId)
        logger.debug { "액세스 토큰 블랙리스트 추가로 인해 ${revokedCount}개의 리프레시 토큰 차단됨 (토큰: ${tokenPreview(token)})" }
        return true
    }

    /**
     * 토큰의 일부분을 보여주는 프리뷰 문자열을 생성합니다.
     */
    private fun tokenPreview(token: String): String = "${token.take(TOKEN_PREVIEW_LENGTH)}..."

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     *
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    fun isBlacklisted(token: String): Boolean =
        runCatching {
            checkTokenIsInBlacklist(token)
        }.getOrElse { exception ->
            logTokenProcessingError(exception, token, "블랙리스트 확인")
        }

    private fun checkTokenIsInBlacklist(token: String): Boolean =
        tokenValidator.getClaims(token).let { claims ->
            when (claims[TYPE_CLAIM]?.toString()) {
                REFRESH_TYPE -> isRefreshTokenInvalid(token)
                else -> false // 액세스 토큰은 별도의 블랙리스트 상태를 관리하지 않음
            }
        }

    private fun logTokenProcessingError(
        exception: Throwable,
        token: String,
        operation: String,
    ): Boolean {
        logger.error(exception) { "토큰 $operation 중 오류 발생: ${tokenPreview(token)}" }
        return false
    }

    private fun isRefreshTokenInvalid(token: String): Boolean =
        refreshTokenDomainService.findByToken(token)?.let { tokenRecord ->
            !tokenRecord.isValid()
        } ?: false

    @Transactional
    fun cleanupExpiredTokens() {
        refreshTokenDomainService.removeExpiredTokens().also { deletedCount ->
            logger.info { "만료된 토큰 $deletedCount 개 삭제됨" }
        }
    }
}
