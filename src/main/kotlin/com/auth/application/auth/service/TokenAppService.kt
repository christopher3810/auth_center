package com.auth.application.auth.service

import com.auth.application.auth.dto.TokenDto
import com.auth.application.auth.dto.UserTokenInfo
import com.auth.domain.auth.factory.AccessTokenFactory
import com.auth.domain.auth.model.TokenPurpose
import com.auth.domain.auth.service.RefreshTokenDomainService
import com.auth.domain.auth.service.TokenGenerator
import com.auth.domain.auth.service.TokenValidator
import com.auth.domain.user.model.User
import com.auth.domain.user.service.UserDomainService
import com.auth.domain.user.value.UserStatus
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExtractionException
import com.auth.exception.UserAccountDeactivatedException
import com.auth.infrastructure.config.JwtConfig
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId.systemDefault

/**
 * 토큰 발급, 갱신, 검증, 무효화 등을 처리하는 애플리케이션 서비스
 *
 * 이 서비스는 도메인 서비스(TokenGenerator, TokenValidator, RefreshTokenService)와
 * 도메인 팩토리(AccessTokenFactory)를 오케스트레이션하여 토큰 관련 유스케이스를 구현합니다.
 */
@Service
class TokenAppService(
    private val tokenGenerator: TokenGenerator,
    private val tokenValidator: TokenValidator,
    private val refreshTokenDomainService: RefreshTokenDomainService,
    private val accessTokenFactory: AccessTokenFactory,
    private val userDomainService: UserDomainService,
    private val jwtConfig: JwtConfig,
) {
    /**
     * 사용자 정보로부터 액세스 토큰과 리프레시 토큰을 생성합니다.
     */
    @Transactional
    fun generateTokens(userInfo: UserTokenInfo): TokenDto =
        with(userInfo) {
            generateTokensInternal(
                subject = email,
                userId = id,
                roles = roles,
            )
        }

    /**
     * User 도메인 객체로부터 액세스 토큰과 리프레시 토큰을 생성합니다.
     */
    @Transactional
    fun generateTokens(user: User): TokenDto =
        with(user) {
            generateTokensInternal(
                subject = email.value,
                userId = id,
                roles = roles,
            )
        }

    /**
     * 토큰 생성 내부 구현
     */
    private fun generateTokensInternal(
        subject: String,
        userId: Long,
        roles: Set<String>,
    ): TokenDto {
        val accessToken =
            tokenGenerator.generateAccessTokenString(
                subject = subject,
                userId = userId,
                roles = roles,
                permissions = emptySet(),
            )

        val refreshToken =
            refreshTokenDomainService.generateRefreshToken(
                subject = subject,
                userId = userId,
            )

        return TokenDto(
            accessToken = accessToken,
            refreshToken = refreshToken.token,
            tokenType = TokenDto.TOKEN_TYPE_BEARER,
            expiresIn = jwtConfig.accessTokenValidityInSeconds,
            refreshTokenExpiresIn = calculateRefreshTokenExpiryInMin(refreshToken.expiryDate),
            refreshTokenIssuedAt = Instant.now(),
            isNewRefreshToken = true,
        )
    }

    /**
     * 리프레시 토큰을 사용하여 새 액세스 토큰과 리프레시 토큰을 발급합니다.
     * 안전한 토큰 교체(Token Rotation) 패턴을 적용합니다.
     */
    @Transactional
    fun refreshAccessToken(refreshTokenValue: String): TokenDto {
        if (!tokenValidator.validateToken(refreshTokenValue)) {
            throw InvalidTokenException("리프레시 토큰 형식이 유효하지 않습니다")
        }

        val refreshTokenModel =
            refreshTokenDomainService.findByToken(refreshTokenValue)
                ?: throw InvalidTokenException("시스템에 리프레시 토큰이 존재하지 않습니다")

        if (!refreshTokenModel.isValid()) {
            throw InvalidTokenException("리프레시 토큰이 이미 사용되었거나 만료되었습니다")
        }

        val subject = tokenValidator.getSubject(refreshTokenValue)
        val userId =
            tokenValidator.getUserId(refreshTokenValue)
                ?: throw TokenExtractionException("토큰에서 사용자 ID를 추출할 수 없습니다")

        val user = validateUserAndGetLatestInfo(userId)

        return createNewTokensAndInvalidateOld(
            subject = subject,
            userId = userId,
            roles = user.roles,
            oldRefreshToken = refreshTokenValue,
        )
    }

    /**
     * 사용자 상태를 검증하고 최신 정보를 가져오는 헬퍼 메서드
     */
    private fun validateUserAndGetLatestInfo(userId: Long): User {
        val user = userDomainService.findUserById(userId)

        if (user.status != UserStatus.ACTIVE) {
            throw UserAccountDeactivatedException(user.status.toString())
        }

        return user
    }

    /**
     * 새 토큰을 발급하고 이전 토큰을 무효화하는 헬퍼 메서드
     */
    private fun createNewTokensAndInvalidateOld(
        subject: String,
        userId: Long,
        roles: Set<String>,
        oldRefreshToken: String,
    ): TokenDto {
        // 새 액세스 토큰 생성
        val newAccessToken =
            tokenGenerator.generateAccessTokenString(
                subject = subject,
                userId = userId,
                roles = roles,
                permissions = emptySet(),
            )

        // 기존 리프레시 토큰을 사용됨으로 표시
        refreshTokenDomainService.markTokenAsUsed(oldRefreshToken)

        // 새 리프레시 토큰 발급
        val newRefreshToken = refreshTokenDomainService.generateRefreshToken(subject, userId)

        // TokenDto 반환
        return TokenDto(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken.token,
            tokenType = TokenDto.TOKEN_TYPE_BEARER,
            expiresIn = jwtConfig.accessTokenValidityInSeconds,
            refreshTokenExpiresIn = calculateRefreshTokenExpiryInMin(newRefreshToken.expiryDate),
            refreshTokenIssuedAt = Instant.now(),
            isNewRefreshToken = true,
        )
    }

    /**
     * 토큰의 유효성을 검증합니다.
     */
    fun validateToken(token: String): Boolean = tokenValidator.validateToken(token)

    /**
     * 리프레시 토큰을 무효화합니다.
     */
    @Transactional
    fun revokeRefreshToken(token: String): Boolean = refreshTokenDomainService.revokeToken(token) != null

    /**
     * 사용자의 모든 리프레시 토큰을 무효화합니다.
     */
    @Transactional
    fun revokeAllUserTokens(userId: Long): Int = refreshTokenDomainService.revokeAllUserTokens(userId)

    /**
     * 토큰에서 사용자 정보를 추출합니다.
     */
    fun getUserInfoFromToken(token: String): UserTokenInfo {
        val claims = tokenValidator.getClaims(token)
        val subject = tokenValidator.getSubject(token)
        val userId =
            tokenValidator.getUserId(token)
                ?: throw TokenExtractionException("User ID not found in token")
        val roles = tokenValidator.getRoles(token)

        val additionalClaims =
            claims.entries
                .filterNot { (key, _) ->
                    key in setOf("sub", "userId", "roles", "exp", "iat", "jti", "type", "permissions")
                }.associate { (key, value) -> key to value }

        return UserTokenInfo(
            id = userId,
            email = subject,
            roles = roles,
            additionalClaims = additionalClaims,
        )
    }

    /**
     * 액세스 토큰 도메인 모델을 생성합니다.
     */
    fun createAccessTokenModel(tokenValue: String): com.auth.domain.auth.model.AccessToken? {
        // 토큰 유효성 검증
        if (!tokenValidator.validateToken(tokenValue)) return null

        // 토큰에서 정보 추출
        val subject = tokenValidator.getSubject(tokenValue)
        val userId = tokenValidator.getUserId(tokenValue) ?: return null
        val roles = tokenValidator.getRoles(tokenValue)
        val permissions = tokenValidator.getPermissions(tokenValue)
        val expiresAt = tokenValidator.getExpirationTime(tokenValue) ?: return null

        // 남은 유효시간 계산 (밀리초 -> 초)
        val remainingValidityInSeconds =
            (expiresAt.toEpochMilli() - System.currentTimeMillis()) / 1000

        // 도메인 모델 생성 및 반환
        return accessTokenFactory.createAccessToken(
            tokenValue = tokenValue,
            userId = userId,
            subject = subject,
            validityInSeconds = remainingValidityInSeconds,
            roles = roles,
            permissions = permissions,
        )
    }

    /**
     * 일회용 토큰을 생성합니다. (이메일 검증, 비밀번호 재설정 등)
     */
    fun generateOneTimeToken(
        email: String,
        userId: Long,
        purpose: TokenPurpose,
    ): String = tokenGenerator.generateOneTimeTokenString(email, userId, purpose)

    /**
     * 만료된 리프레시 토큰을 정리합니다.
     */
    @Transactional
    fun cleanupExpiredTokens(): Int = refreshTokenDomainService.removeExpiredTokens()

    /**
     * LocalDateTime 으로 표현된 만료시간을 현재 시점부터 남은 분으로 계산
     */
    private fun calculateRefreshTokenExpiryInMin(expiryDate: LocalDateTime): Long {
        val expiryInstant = expiryDate.atZone(systemDefault()).toInstant()
        val nowInstant = Instant.now()

        return ((expiryInstant.epochSecond - nowInstant.epochSecond) / 60).coerceAtLeast(0)
    }
}
