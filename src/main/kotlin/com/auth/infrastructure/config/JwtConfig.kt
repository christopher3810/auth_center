package com.auth.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

/**
 * JWT 관련 설정값
 *
 * @property secret JWT 토큰 서명에 사용될 비밀키
 * @property accessTokenValidityInSeconds 액세스 토큰 유효 기간(초)
 * @property refreshTokenValidityInSeconds 리프레시 토큰 유효 기간(초)
 */
@ConfigurationProperties(prefix = "jwt")
data class JwtConfig @ConstructorBinding constructor(
    val secret: String,
    val accessTokenValidityInSeconds: Long,
    val refreshTokenValidityInSeconds: Long
) {
    /**
     * 액세스 토큰 유효 기간(밀리초)
     * TokenService와의 일관성을 위해 제공
     */
    val expirationMs: Long
        get() = accessTokenValidityInSeconds * 1000
    
    /**
     * 리프레시 토큰 유효 기간(밀리초) 
     */
    val refreshTokenExpirationMs: Long
        get() = refreshTokenValidityInSeconds * 1000
}