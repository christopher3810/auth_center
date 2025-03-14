package com.auth.infrastructure.security.token

import com.auth.infrastructure.config.JwtConfig
import com.auth.domain.auth.model.TokenPurpose
import com.auth.domain.auth.service.TokenGenerator
import com.auth.domain.auth.service.TokenValidator
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExpiredException
import com.auth.exception.TokenExtractionException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

/**
 * 주요기능 구현체 DI를 통해서 사용된다.
 */
@Component
class JwtTokenAdaptor(
    private val jwtConfig: JwtConfig = JwtConfig.standard()
): TokenGenerator, TokenValidator {
    // 문자열을 바이트 배열로 변환하여 적절한 Key 인스턴스 생성
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray(Charsets.UTF_8))


    /**
     * 사용자의 이메일을 기반으로 JWT 토큰을 생성합니다.
     *
     * @param subject 사용자의 이메일
     * @return 생성된 JWT 토큰
     */
    override fun generateAccessToken(subject: String): String {
        return generateAccessTokenBuilder(subject).build()
    }

    /**
     * 사용자의 이메일을 기반으로 리프레시 토큰을 생성합니다.
     *
     * @param subject 사용자의 이메일
     * @return 생성된 리프레시 토큰
     */
    override fun generateRefreshToken(subject: String): String {
        return generateRefreshTokenBuilder(subject).build()
    }

    /**
     * 사용자의 이메일을 기반으로 일회용 토큰을 생성합니다.
     *
     * @param subject 사용자의 이메일
     * @return 생성된 리프레시 토큰
     */
    override fun generateOneTimeToken(subject: String, purpose: TokenPurpose): String {
        return generateOneTimeTokenBuilder(subject, purpose).build()
    }

    /**
     * 권한 검증용 토큰 빌더를 생성합니다.
     *
     * @param subject 토큰의 주체(일반적으로 사용자 이메일)
     * @return TokenBuilder 인스턴스
     */
    override fun generateAccessTokenBuilder(subject: String): TokenBuilder {
        return TokenBuilder.authorizationTokenBuilder(subject, jwtConfig.refreshExpirationMs, key)
    }

    /**
     * 리프레시 토큰 빌더를 생성합니다.
     *
     * @param subject 토큰의 주체(일반적으로 사용자 이메일)
     * @return TokenBuilder 인스턴스
     */
    override fun generateRefreshTokenBuilder(subject: String): TokenBuilder {
        return TokenBuilder.refreshTokenBuilder(subject, jwtConfig.refreshExpirationMs, key)
    }

    /**
     * 일회용 토큰 빌더를 생성합니다.
     *
     * @param subject 토큰의 주체(일반적으로 사용자 이메일)
     * @param purpose 토큰의 목적
     * @return TokenBuilder 인스턴스
     */
    override fun generateOneTimeTokenBuilder(subject: String, purpose: TokenPurpose): TokenBuilder {
        val oneTimeExpirationMs = jwtConfig.expirationMs / 2
        return TokenBuilder.oneTimeTokenBuilder(subject, oneTimeExpirationMs, key, purpose.value)
    }

    /**
     * JWT 토큰을 파싱하여 Claims를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에 포함된 Claims 객체
     * @throws InvalidTokenException 토큰이 유효하지 않거나 파싱에 실패한 경우
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
     *
     * @param token JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
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
     * JWT 토큰에서 사용자의 이메일(subject)을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 subject (일반적으로 사용자 이메일)
     */
    override fun getUsername(token: String): String {
        return getClaims(token).subject
    }

    /**
     * JWT 토큰에서 토큰의 목적(purpose) 클레임을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에 포함된 purpose 값 (없으면 null)
     */
    override fun getPurpose(token: String): String? {
        return getClaims(token)["purpose"] as? String
    }

} 