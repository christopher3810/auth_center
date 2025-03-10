package com.auth.infrastructure.security.token

import com.auth.infrastructure.config.JwtConfig
import com.auth.domain.auth.model.TokenPurpose
import com.auth.domain.auth.service.TokenBuilder
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import javax.crypto.SecretKey

/**
 * JWT 토큰 생성 및 검증을 담당하는 애플리케이션 서비스
 */
@Service
class TokenProvider(
    private val jwtConfig: JwtConfig = JwtConfig.standard()
) {
    // 문자열을 바이트 배열로 변환하여 적절한 Key 인스턴스 생성
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray(Charsets.UTF_8))

    /**
     * 사용자의 이메일을 기반으로 JWT 토큰을 생성합니다.
     *
     * @param email 사용자의 이메일
     * @return 생성된 JWT 토큰
     */
    fun generateToken(email: String): String {
        return createAccessTokenBuilder(email).build()
    }

    /**
     * 사용자의 이메일을 기반으로 리프레시 토큰을 생성합니다.
     *
     * @param email 사용자의 이메일
     * @return 생성된 리프레시 토큰
     */
    fun generateRefreshToken(email: String): String {
        return createRefreshTokenBuilder(email).build()
    }

    /**
     * JWT 토큰에서 사용자의 이메일을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자의 이메일
     */
    fun getUsernameFromJWT(token: String): String {
        val claims: Claims = getClaimsFromToken(token)
        return claims.subject
    }

    /**
     * JWT 토큰에서 클레임을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 클레임
     */
    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     *
     * @param authToken JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    fun validateToken(authToken: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(authToken)
            true
        } catch (ex: Exception) {
            // 예외 처리: 로그 기록, 특정 예외에 따른 재처리 고려
            false
        }
    }

    /**
     * 액세스 토큰 빌더를 생성합니다.
     *
     * @param subject 토큰의 주체(일반적으로 사용자 이메일)
     * @return TokenBuilder 인스턴스
     */
    fun createAccessTokenBuilder(subject: String): TokenBuilder {
        return TokenBuilder.accessTokenBuilder(subject, jwtConfig.expirationMs, key)
    }

    /**
     * 리프레시 토큰 빌더를 생성합니다.
     *
     * @param subject 토큰의 주체(일반적으로 사용자 이메일)
     * @return TokenBuilder 인스턴스
     */
    fun createRefreshTokenBuilder(subject: String): TokenBuilder {
        return TokenBuilder.refreshTokenBuilder(subject, jwtConfig.refreshExpirationMs, key)
    }

    /**
     * 권한 검증용 토큰 빌더를 생성합니다.
     *
     * @param subject 토큰의 주체(일반적으로 사용자 이메일)
     * @return TokenBuilder 인스턴스
     */
    fun createAuthorizationTokenBuilder(subject: String): TokenBuilder {
        return TokenBuilder.authorizationTokenBuilder(subject, jwtConfig.expirationMs, key)
    }

    /**
     * 일회용 토큰 빌더를 생성합니다.
     *
     * @param subject 토큰의 주체(일반적으로 사용자 이메일)
     * @param purpose 토큰의 목적
     * @return TokenBuilder 인스턴스
     */
    fun createOneTimeTokenBuilder(subject: String, purpose: TokenPurpose): TokenBuilder {
        // 일회용 토큰은 일반적으로 더 짧은 만료 시간을 가짐
        val oneTimeExpirationMs = jwtConfig.expirationMs / 2
        return TokenBuilder.oneTimeTokenBuilder(subject, oneTimeExpirationMs, key, purpose.value)
    }
} 