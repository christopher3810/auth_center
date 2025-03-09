package com.auth.controller

import com.auth.controller.dto.request.LoginRequest
import com.auth.controller.dto.request.TokenRefreshRequest
import com.auth.controller.dto.request.TokenValidationRequest
import com.auth.controller.dto.response.TokenResponse
import com.auth.controller.dto.response.TokenValidationResponse
import com.auth.service.TokenProvider
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * 인증 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val tokenProvider: TokenProvider
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    /**
     * 로그인 및 토큰 발급
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): Mono<ResponseEntity<TokenResponse>> {
        logger.debug("로그인 요청: {}", loginRequest.username)
        
        return authService.login(loginRequest)
            .map { tokenResponse -> 
                ResponseEntity.ok(tokenResponse) 
            }
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody refreshRequest: TokenRefreshRequest): Mono<ResponseEntity<TokenResponse>> {
        logger.debug("토큰 갱신 요청")
        
        return authService.refreshToken(refreshRequest)
            .map { tokenResponse -> 
                ResponseEntity.ok(tokenResponse) 
            }
    }

    /**
     * 로그아웃 (토큰 무효화)
     */
    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authorization: String): Mono<ResponseEntity<Void>> {
        val token = authorization.substring(7) // "Bearer " 제거
        logger.debug("로그아웃 요청")
        
        return authService.logout(token)
            .map { success ->
                if (success) {
                    ResponseEntity<Void>(HttpStatus.OK)
                } else {
                    ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }
    }

    /**
     * 토큰 검증
     */
    @PostMapping("/validate")
    fun validateToken(@Valid @RequestBody request: TokenValidationRequest): Mono<ResponseEntity<TokenValidationResponse>> {
        logger.debug("토큰 검증 요청")
        
        return authService.validateToken(request.token)
            .map { isValid ->
                if (isValid) {
                    val username = tokenProvider.getUsernameFromJWT(request.token)
                    val userId = tokenProvider.getUserIdFromJWT(request.token)
                    
                    ResponseEntity.ok(
                        TokenValidationResponse(
                        valid = true,
                        username = username,
                        userId = userId
                    )
                    )
                } else {
                    ResponseEntity.ok(
                        TokenValidationResponse(
                        valid = false
                    )
                    )
                }
            }
    }
} 