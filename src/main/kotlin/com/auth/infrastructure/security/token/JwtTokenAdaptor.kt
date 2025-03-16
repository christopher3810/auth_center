package com.auth.infrastructure.security.token

import com.auth.domain.auth.model.TokenPurpose
import com.auth.domain.auth.model.TokenType
import com.auth.domain.auth.service.TokenGenerator
import com.auth.domain.auth.service.TokenValidator
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExpiredException
import com.auth.exception.TokenExtractionException
import com.auth.infrastructure.config.JwtConfig
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.time.Instant
import javax.crypto.SecretKey

/**
 * JWT 토큰 관련 기능을 구현하는 어댑터 클래스
 *
 * 이 클래스는 인프라 계층에 위치하며, 토큰 문자열 생성 및 검증에 집중합니다.
 * 도메인 모델에 직접 의존하지 않고, 토큰 생성에 필요한 최소한의 정보만 받아 처리합니다.
 */
@Component
class JwtTokenAdaptor(
    private val jwtConfig: JwtConfig
) : TokenGenerator, TokenValidator {
    // 문자열을 바이트 배열로 변환하여 적절한 Key 인스턴스 생성
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray(Charsets.UTF_8))
    private val USE_ID: String = "userId"

    /**
     * 사용자 정보로부터 액세스 토큰 문자열 생성
     */
    override fun generateAccessTokenString(
        subject: String, userId: Long,
        roles: Set<String>, permissions: Set<String>
    ): String {

        val tokenBuilder = TokenBuilder.authorizationTokenBuilder(
            subject, jwtConfig.expirationMs, key, roles, permissions
        )
        return tokenBuilder
            .withClaim(USE_ID , userId)
            .build()
    }

    /**
     * 사용자 정보로부터 리프레시 토큰 문자열 생성
     */
    override fun generateRefreshTokenString(subject: String, userId: Long): String {
        val tokenBuilder = TokenBuilder.refreshTokenBuilder(
            subject, jwtConfig.refreshTokenExpirationMs, key
        )
        return tokenBuilder
            .withClaim(USE_ID, userId)
            .build()
    }

    /**
     * 사용자 정보로부터 일회용 토큰 문자열 생성
     */
    override fun generateOneTimeTokenString(subject: String, userId: Long, purpose: TokenPurpose): String {
        val tokenBuilder = TokenBuilder.oneTimeTokenBuilder(
            subject, jwtConfig.expirationMs, key, purpose.value
        )
        return tokenBuilder
            .withClaim(USE_ID, userId)
            .build()
    }

    /**
     * JWT 토큰을 파싱하여 Claims를 추출합니다.
     */
    override fun getClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (ex: ExpiredJwtException) {
            throw TokenExpiredException("토큰이 만료되었습니다: ${ex.message}")
        } catch (ex: JwtException) {
            throw InvalidTokenException("토큰이 유효하지 않습니다: ${ex.message}")
        } catch (ex: Exception) {
            throw TokenExtractionException("토큰에서 정보를 추출할 수 없습니다: ${ex.message}")
        }
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     */
    override fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * JWT 토큰에서 사용자의 식별자(subject)를 추출합니다.
     */
    override fun getSubject(token: String): String {
        return getClaims(token).subject
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출합니다.
     */
    override fun getUserId(token: String): Long? {
        val userId = getClaims(token)[USE_ID] ?: return null
        return when (userId) {
            is Number -> userId.toLong()
            is String -> userId.toLong()
            else -> null
        }
    }

    /**
     * JWT 토큰에서 토큰의 목적(purpose) 클레임을 추출합니다.
     */
    override fun getPurpose(token: String): TokenPurpose? {
        val purposeStr = getClaims(token)["purpose"] as? String ?: return null
        return TokenPurpose.fromValue(purposeStr)
    }

    /**
     * JWT 토큰에서 토큰 타입을 추출합니다.
     */
    override fun getTokenType(token: String): TokenType? {
        val typeStr = getClaims(token)["type"] as? String ?: return null
        return TokenType.fromValue(typeStr)
    }

    /**
     * JWT 토큰에서 역할 정보를 추출합니다.
     */
    override fun getRoles(token: String): Set<String> {
        val roles = getClaims(token)["roles"] as? String
        return roles?.split(",")?.toSet() ?: emptySet()
    }

    /**
     * JWT 토큰에서 권한 정보를 추출합니다.
     */
    override fun getPermissions(token: String): Set<String> {
        val permissions = getClaims(token)["permissions"] as? String
        return permissions?.split(",")?.toSet() ?: emptySet()
    }

    /**
     * JWT 토큰에서 만료 시간을 추출합니다.
     */
    override fun getExpirationTime(token: String): Instant? {
        val expiration = getClaims(token).expiration
        return expiration?.toInstant()
    }

} 