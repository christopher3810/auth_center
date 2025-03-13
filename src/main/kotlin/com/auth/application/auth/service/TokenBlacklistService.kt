package com.auth.application.auth.service

import com.auth.domain.auth.repository.RefreshTokenRepository
import com.auth.infrastructure.security.token.JwtTokenAdaptor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * 토큰 블랙리스트 관리 애플리케이션 서비스
 * 
 * 애플리케이션 계층에 위치하여 도메인 로직을 조정하고 외부 서비스와 연동합니다.
 */
@Service
class TokenBlacklistService(
    private val jwtTokenAdaptor: JwtTokenAdaptor,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    private val logger = LoggerFactory.getLogger(TokenBlacklistService::class.java)
    
    /**
     * 토큰을 블랙리스트에 추가합니다.
     * 
     * @param token JWT 토큰
     * @return 처리 결과
     */
    @Transactional
    fun addToBlacklist(token: String): Boolean {
        return try {
            // 토큰 검증
            if (!jwtTokenAdaptor.validateToken(token)) {
                logger.warn("블랙리스트에 추가 실패: 유효하지 않은 토큰")
                return false
            }
            
            // 토큰에서 사용자 정보 추출
            val claims = jwtTokenAdaptor.getClaims(token)
            val userId = claims["userId"]?.toString()?.toLongOrNull()
            
            if (userId == null) {
                logger.warn("블랙리스트에 추가 실패: 토큰에 사용자 ID가 없음")
                return false
            }
            
            // 리프레시 토큰인 경우 데이터베이스에서 찾아서 차단 상태로 변경
            val tokenStr = token.trim()
            val refreshToken = refreshTokenRepository.findByToken(tokenStr)
            
            if (refreshToken.isPresent) {
                val token = refreshToken.get()
                token.revoke()
                refreshTokenRepository.save(token)
                logger.debug("리프레시 토큰 블랙리스트 추가 성공: {}", tokenStr)
                return true
            }
            
            // 액세스 토큰인 경우 모든 사용자 리프레시 토큰을 차단하여 재로그인 유도
            val revokedCount = refreshTokenRepository.revokeAllUserTokens(userId)
            logger.debug("액세스 토큰 블랙리스트 추가로 인해 {}개의 리프레시 토큰 차단됨", revokedCount)
            
            return true
        } catch (e: Exception) {
            logger.error("토큰 블랙리스트 추가 중 오류 발생: {}", e.message)
            return false
        }
    }
    
    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     * 
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    fun isBlacklisted(token: String): Boolean {
        return try {
            // 토큰에서 사용자 정보 추출
            val claims = jwtTokenAdaptor.getClaims(token)
            val tokenType = claims["type"]?.toString()
            
            // 리프레시 토큰인 경우 데이터베이스에서 확인
            if (tokenType == "refresh") {
                val refreshToken = refreshTokenRepository.findByToken(token)
                return refreshToken.map { !it.isValid() }.orElse(false)
            }
            
            // 액세스 토큰은 여기서는 revoke 상태를 확인할 수 없으므로 false 반환
            // 실제 구현에서는 Redis 등을 사용하여 액세스 토큰의 블랙리스트 상태를 확인해야 함
            return false
        } catch (e: Exception) {
            logger.error("토큰 블랙리스트 확인 중 오류 발생: {}", e.message)
            return false
        }
    }
    
    /**
     * 만료된 토큰을 데이터베이스에서 정리합니다.
     */
    @Transactional
    fun cleanupExpiredTokens() {
        val now = LocalDateTime.now()
        val deletedCount = refreshTokenRepository.deleteAllExpiredTokens(now)
        logger.info("만료된 토큰 {}개 삭제됨", deletedCount)
    }
    
    /**
     * Instant를 LocalDateTime으로 변환
     */
    private fun Instant.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    }
} 