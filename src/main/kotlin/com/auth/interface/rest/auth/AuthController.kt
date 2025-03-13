package com.auth.`interface`.rest.auth

import com.auth.`interface`.rest.dto.TokenResponse
import com.auth.`interface`.rest.dto.TokenValidationResponse
import com.auth.application.auth.service.TokenBlacklistService
import com.auth.infrastructure.security.token.JwtTokenAdaptor
import com.auth.`interface`.rest.dto.LoginRequest
import com.auth.`interface`.rest.dto.TokenRefreshRequest
import com.auth.`interface`.rest.dto.TokenValidationRequest
import com.auth.`interface`.rest.dto.LogoutRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 API 컨트롤러
 * 인터페이스 계층에서 사용자 요청을 애플리케이션 서비스로 라우팅합니다.
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val jwtTokenAdaptor: JwtTokenAdaptor,
    private val tokenBlacklistService: TokenBlacklistService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    /**
     * 로그인 및 토큰 발급
     */
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<TokenResponse> {
        // 실제 구현에서는 사용자 인증 서비스를 호출하여 로그인 처리
        logger.info("로그인 요청: {}", loginRequest.username)
        
        // 예시 구현 - 실제로는 사용자 인증 후 토큰 발급
        val accessToken = jwtTokenAdaptor.generateAccessToken(loginRequest.username)
        val refreshToken = jwtTokenAdaptor.generateRefreshToken(loginRequest.username)
        
        val response = TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = 3600
        )
        
        return ResponseEntity.ok(response)
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    fun refreshToken(@RequestBody refreshRequest: TokenRefreshRequest): ResponseEntity<TokenResponse> {
        logger.info("토큰 갱신 요청")
        
        // 토큰 유효성 검증
        if (!jwtTokenAdaptor.validateToken(refreshRequest.refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        
        // 사용자 정보 추출
        val username = jwtTokenAdaptor.getUsername(refreshRequest.refreshToken)
        
        // 새 액세스 토큰 발급
        val accessToken = jwtTokenAdaptor.generateAccessToken(username)
        
        val response = TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshRequest.refreshToken,
            expiresIn = 3600
        )
        
        return ResponseEntity.ok(response)
    }

    /**
     * 로그아웃 (토큰 무효화)
     */
    @PostMapping("/logout")
    fun logout(@RequestBody request: LogoutRequest): ResponseEntity<Void> {
        logger.info("로그아웃 요청")
        
        val blacklisted = tokenBlacklistService.addToBlacklist(request.token)
        
        return if (blacklisted) {
            ResponseEntity.ok().build()
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * 토큰 검증
     */
    @PostMapping("/validate")
    fun validateToken(@RequestBody request: TokenValidationRequest): ResponseEntity<TokenValidationResponse> {
        logger.info("토큰 검증 요청")
        
        val isValid = jwtTokenAdaptor.validateToken(request.token)
        
        val response = if (isValid) {
            val username = jwtTokenAdaptor.getUsername(request.token)
            val claims = jwtTokenAdaptor.getClaims(request.token)
            val userId = claims["userId"]?.toString()
            val roles = claims["roles"]?.toString()?.split(",") ?: emptyList()
            
            TokenValidationResponse(
                valid = true,
                username = username,
                authorities = roles
            )
        } else {
            TokenValidationResponse(
                valid = false,
                error = "유효하지 않은 토큰입니다."
            )
        }
        
        return ResponseEntity.ok(response)
    }
} 