package com.auth.application.auth.service

import com.auth.application.auth.dto.TokenDto
import com.auth.application.auth.dto.UserTokenInfo
import com.auth.domain.auth.factory.AccessTokenFactory
import com.auth.domain.auth.model.RefreshToken
import com.auth.domain.auth.model.TokenPurpose
import com.auth.domain.auth.service.RefreshTokenDomainService
import com.auth.domain.auth.service.TokenGenerator
import com.auth.domain.auth.service.TokenValidator
import com.auth.domain.user.model.User
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExtractionException
import com.auth.infrastructure.config.JwtConfig
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId.systemDefault
import java.util.Optional

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
    private val jwtConfig: JwtConfig
) {

    /**
     * 사용자 정보로부터 액세스 토큰과 리프레시 토큰을 생성합니다.
     * 리프레시 토큰은 데이터베이스에 저장됩니다.
     */
    @Transactional
    fun generateTokens(userInfo: UserTokenInfo): TokenDto {
        return generateTokensInternal(
            subject = userInfo.email,
            userId = userInfo.id,
            roles = userInfo.roles
        )
    }

    /**
     * User 도메인 객체로부터 액세스 토큰과 리프레시 토큰을 생성합니다.
     * 리프레시 토큰은 데이터베이스에 저장됩니다.
     */
    @Transactional
    fun generateTokens(user: User): TokenDto {
        return generateTokensInternal(
            subject = user.email.value,
            userId = user.id,
            roles = user.roles
        )
    }

    /**
     * 토큰 생성 내부 구현
     * 액세스 토큰과 리프레시 토큰을 생성하고 TokenDto로 반환합니다.
     */
    private fun generateTokensInternal(
        subject: String,
        userId: Long,
        roles: Set<String>
    ): TokenDto {
        // 1. 액세스 토큰 생성
        val accessToken = tokenGenerator.generateAccessTokenString(
            subject = subject,
            userId = userId,
            roles = roles,
            permissions = emptySet()
        )
        
        // 2. 리프레시 토큰 생성 및 저장
        val refreshToken = refreshTokenDomainService.generateRefreshToken(
            subject = subject,
            userId = userId
        )

        // 3. TokenDto 반환
        return TokenDto(
            accessToken = accessToken,
            refreshToken = refreshToken.token,
            tokenType = TokenDto.TOKEN_TYPE_BEARER,
            expiresIn = jwtConfig.accessTokenValidityInSeconds,
            refreshTokenExpiresIn = calculateRefreshTokenExpiryInMin(refreshToken.expiryDate),
            refreshTokenIssuedAt = Instant.now(),
            isNewRefreshToken = true
        )
    }

    /**
     * 리프레시 토큰을 사용하여 새 액세스 토큰을 발급합니다.
     * 기존 리프레시 토큰은 사용됨으로 표시하고 필요에 따라 새 리프레시 토큰을 발급할 수 있습니다.
     */
    @Transactional
    fun refreshAccessToken(refreshTokenValue: String, issueNewRefreshToken: Boolean = false): TokenDto {
        // 1. 리프레시 토큰 유효성 검증
        if (!tokenValidator.validateToken(refreshTokenValue)) {
            throw InvalidTokenException("Invalid refresh token")
        }
        
        // 2. 도메인 모델 조회
        val refreshTokenModel = refreshTokenDomainService.findByToken(refreshTokenValue)
            .orElseThrow { InvalidTokenException("Refresh token not found in the system") }
        
        // 3. 도메인 모델 유효성 검증
        if (!refreshTokenModel.isValid()) {
            throw InvalidTokenException("Refresh token is no longer valid")
        }
        
        // 4. 토큰에서 사용자 정보 추출
        val subject = tokenValidator.getSubject(refreshTokenValue)
        val userId = tokenValidator.getUserId(refreshTokenValue) 
            ?: throw TokenExtractionException("User ID not found in token")
        val roles = tokenValidator.getRoles(refreshTokenValue)
        
        // 5. 새 액세스 토큰 생성
        val newAccessToken = tokenGenerator.generateAccessTokenString(
            subject = subject,
            userId = userId,
            roles = roles,
            permissions = emptySet()
        )

        // 6. 현재 리프레시 토큰을 사용됨으로 표시
        refreshTokenDomainService.markTokenAsUsed(refreshTokenValue)
        
        // 7. 새 리프레시 토큰 발급 (요청된 경우)
        val finalRefreshToken: RefreshToken = if (issueNewRefreshToken) {
            refreshTokenDomainService.generateRefreshToken(subject, userId)
        } else {
            refreshTokenModel
        }
        
        // 8. TokenDto 반환
        return TokenDto(
            accessToken = newAccessToken,
            refreshToken = finalRefreshToken.token,
            tokenType = TokenDto.TOKEN_TYPE_BEARER,
            expiresIn = jwtConfig.accessTokenValidityInSeconds,
            refreshTokenExpiresIn = calculateRefreshTokenExpiryInMin(finalRefreshToken.expiryDate),
            refreshTokenIssuedAt = if (issueNewRefreshToken) java.time.Instant.now() else null,
            isNewRefreshToken = issueNewRefreshToken
        )
    }

    /**
     * 토큰의 유효성을 검증합니다.
     */
    fun validateToken(token: String): Boolean {
        return tokenValidator.validateToken(token)
    }
    
    /**
     * 리프레시 토큰을 무효화합니다.
     */
    @Transactional
    fun revokeRefreshToken(token: String): Boolean {
        return refreshTokenDomainService.revokeToken(token).isPresent
    }
    
    /**
     * 사용자의 모든 리프레시 토큰을 무효화합니다.
     */
    @Transactional
    fun revokeAllUserTokens(userId: Long): Int {
        return refreshTokenDomainService.revokeAllUserTokens(userId)
    }

    /**
     * 토큰에서 사용자 정보를 추출합니다.
     */
    fun getUserInfoFromToken(token: String): UserTokenInfo {
        val claims = tokenValidator.getClaims(token)
        val subject = tokenValidator.getSubject(token)
        val userId = tokenValidator.getUserId(token) 
            ?: throw TokenExtractionException("User ID not found in token")
        val roles = tokenValidator.getRoles(token)

        val additionalClaims = claims.entries
            .filter { (key, _) -> 
                key != "sub" && key != "userId" && key != "roles" && 
                key != "exp" && key != "iat" && key != "jti" &&
                key != "type" && key != "permissions"
            }
            .associate { (key, value) -> key to value }
        
        return UserTokenInfo(
            id = userId,
            email = subject,
            roles = roles,
            additionalClaims = additionalClaims
        )
    }
    
    /**
     * 액세스 토큰 도메인 모델을 생성합니다.
     * 이 메서드는 인증 필터나 다른 애플리케이션 레이어에서 
     * 토큰 문자열로부터 접근 가능한 도메인 모델이 필요한 경우 사용됩니다.
     */
    fun createAccessTokenModel(tokenValue: String): Optional<com.auth.domain.auth.model.AccessToken> {
        if (!tokenValidator.validateToken(tokenValue)) {
            return Optional.empty()
        }
        
        val subject = tokenValidator.getSubject(tokenValue)
        val userId = tokenValidator.getUserId(tokenValue) ?: return Optional.empty()
        val roles = tokenValidator.getRoles(tokenValue)
        val permissions = tokenValidator.getPermissions(tokenValue)
        val expiresAt = tokenValidator.getExpirationTime(tokenValue) ?: return Optional.empty()
        
        val token = accessTokenFactory.createAccessToken(
            tokenValue = tokenValue,
            userId = userId,
            subject = subject,
            validityInSeconds = (expiresAt.toEpochMilli() - System.currentTimeMillis()) / 1000,
            roles = roles,
            permissions = permissions
        )
        
        return Optional.of(token)
    }
    
    /**
     * 일회용 토큰을 생성합니다. (이메일 검증, 비밀번호 재설정 등)
     */
    fun generateOneTimeToken(email: String, userId: Long, purpose: TokenPurpose): String {
        return tokenGenerator.generateOneTimeTokenString(email, userId, purpose)
    }
    
    /**
     * 만료된 리프레시 토큰을 정리합니다.
     * 이 메서드는 스케줄링된 작업에서 주기적으로 호출될 수 있습니다.
     */
    @Transactional
    fun cleanupExpiredTokens(): Int {
        return refreshTokenDomainService.removeExpiredTokens()
    }

    /**
     * LocalDateTime 으로 표현된 만료시간을 현재 시점부터 남은 분으로 계산
     */
    private fun calculateRefreshTokenExpiryInMin(expiryDate: LocalDateTime): Long {
        val expiryInstant = expiryDate.atZone(systemDefault()).toInstant()
        val nowInstant = Instant.now()

        return ((expiryInstant.epochSecond - nowInstant.epochSecond) / 60).coerceAtLeast(0)
    }
}