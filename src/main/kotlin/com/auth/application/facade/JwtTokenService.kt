package com.auth.application.facade

import com.auth.application.auth.dto.TokenDto
import com.auth.infrastructure.security.token.JwtTokenAdaptor
import com.auth.domain.auth.model.TokenClaim
import com.auth.infrastructure.config.JwtConfig
import com.auth.application.auth.dto.UserTokenInfo
import com.auth.domain.auth.service.TokenGenerator
import com.auth.domain.auth.service.TokenValidator
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExtractionException

/**
 * JWT 토큰 관련 기능을 제공하는 서비스 클래스
 */
class JwtTokenService(
    private val tokenGenerator: TokenGenerator,
    private val tokenValidator: TokenValidator,
    private val jwtConfig: JwtConfig = JwtConfig.standard()
) {

    /**
     * 사용자 정보를 기반으로 토큰을 생성합니다.
     *
     * @param userInfo 토큰에 포함될 사용자 정보
     * @return 생성된 토큰 응답
     */
    fun generateTokens(userInfo: UserTokenInfo): TokenDto {
        // 액세스 토큰 생성
        val authorizationTokenBuilder = tokenGenerator.generateAccessTokenBuilder(userInfo.email)
            .withClaim(TokenClaim.USER_ID.value, userInfo.id)

        // 역할 정보가 있으면 추가
        if (userInfo.roles.isNotEmpty()) {
            authorizationTokenBuilder.withClaim(TokenClaim.ROLES.value, userInfo.roles.joinToString(","))
        }

        // 추가 클레임이 있으면 추가
        userInfo.additionalClaims.forEach { (key, value) ->
            authorizationTokenBuilder.withClaim(key, value.toString())
        }

        val accessToken = authorizationTokenBuilder.build()

        // 리프레시 토큰 생성
        val refreshToken = tokenGenerator.generateRefreshTokenBuilder(userInfo.email)
            .withClaim(TokenClaim.USER_ID.value, userInfo.id)
            .build()

        return TokenDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtConfig.expirationMs / 1000 // 밀리초를 초로 변환
        )
    }

    /**
     * 리프레시 토큰을 사용하여 액세스 토큰을 갱신합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 갱신된 토큰 응답
     */
    fun refreshToken(refreshToken: String): TokenDto {
        // 리프레시 토큰 검증
        if (!tokenValidator.validateToken(refreshToken)) {
            throw InvalidTokenException("Invalid refresh token")
        }

        // 토큰에서 사용자 정보 추출
        val userInfo = getUserInfoFromToken(refreshToken)

        // 새 액세스 토큰 생성
        val authorizationTokenBuilder = tokenGenerator.generateAccessTokenBuilder(userInfo.email)
            .withClaim(TokenClaim.USER_ID.value, userInfo.id)

        // 역할 정보가 있으면 추가
        if (userInfo.roles.isNotEmpty()) {
            authorizationTokenBuilder.withClaim(TokenClaim.ROLES.value, userInfo.roles.joinToString(","))
        }

        // 추가 클레임이 있으면 추가
        userInfo.additionalClaims.forEach { (key, value) ->
            authorizationTokenBuilder.withClaim(key, value.toString())
        }

        val accessToken = authorizationTokenBuilder.build()

        return TokenDto(
            accessToken = accessToken,
            refreshToken = refreshToken, // 기존 리프레시 토큰 유지
            expiresIn = jwtConfig.expirationMs / 1000 // 밀리초를 초로 변환
        )
    }

    /**
     * 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     */
    fun validateToken(token: String): Boolean {
        return tokenValidator.validateToken(token)
    }

    /**
     * 토큰에서 사용자 정보를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자 정보
     */
    fun getUserInfoFromToken(token: String): UserTokenInfo {
        val claims = tokenValidator.getClaims(token)

        val email = claims.subject
        val userId = claims[TokenClaim.USER_ID.value]?.toString()?.toLongOrNull()
            ?: throw TokenExtractionException("User ID not found in token")

        // 역할 정보 추출
        val roles = claims[TokenClaim.ROLES.value]?.toString()
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()

        // 추가 클레임 추출 (USER_ID와 ROLES를 제외한 나머지)
        val additionalClaims = claims.entries
            .filter { it.key != TokenClaim.USER_ID.value && it.key != TokenClaim.ROLES.value && !isReservedClaim(it.key) }
            .associate { it.key to it.value }

        return UserTokenInfo(
            id = userId,
            email = email,
            roles = roles,
            additionalClaims = additionalClaims
        )
    }

    /**
     * JWT 예약 클레임인지 확인합니다.
     */
    private fun isReservedClaim(claim: String): Boolean {
        return claim == "sub" || claim == "iat" || claim == "exp" || claim == "nbf" || claim == "iss" || claim == "aud" || claim == "jti"
    }
}