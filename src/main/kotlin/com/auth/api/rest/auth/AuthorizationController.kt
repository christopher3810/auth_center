package com.auth.api.rest.auth

import com.auth.api.docs.annotations.ApiForbiddenError
import com.auth.api.docs.annotations.ApiInvalidTokenError
import com.auth.api.docs.annotations.ApiServerError
import com.auth.api.docs.annotations.ApiUserNotFoundError
import com.auth.api.rest.dto.TokenRefreshRequest
import com.auth.api.security.annotation.ResourceOwnerOrAdmin
import com.auth.application.auth.dto.TokenDto
import com.auth.application.auth.service.TokenAppService
import com.auth.application.user.service.UserAccountAppService
import com.auth.domain.auth.model.TokenPurpose
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Tag(name = "Authorization", description = "인가 및 계정 관리 API - 토큰 관리, 계정 상태 변경 등")
@RestController
@RequestMapping("/api/auth")
class AuthorizationController(
    private val userAccountAppService: UserAccountAppService,
    private val tokenAppService: TokenAppService,
) {
    @Operation(
        summary = "액세스 토큰 재발급",
        description = "유효한 리프레시 토큰을 사용하여 새 액세스 토큰과 리프레시 토큰을 발급합니다.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "토큰 재발급 성공",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = TokenDto::class),
                examples = [
                    ExampleObject(
                        name = "success",
                        summary = "토큰 재발급 성공 응답",
                        value = """
                        {
                          "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "token_type": "bearer",
                          "expires_in": 3600,
                          "refresh_token_expires_in": 604800
                        }
                        """,
                    ),
                ],
            ),
        ],
    )
    @ApiInvalidTokenError
    @ApiServerError
    @PostMapping("v1/token/refresh")
    fun refreshToken(
        @Valid @RequestBody request: TokenRefreshRequest,
    ): TokenDto {
        logger.debug { "토큰 재발급 요청: ${request.refreshToken.take(10)}..." }
        return tokenAppService.refreshAccessToken(request.refreshToken)
    }

    @ResourceOwnerOrAdmin
    @Operation(
        summary = "일회성 토큰 발행",
        description = "이메일 인증, 비밀번호 재설정 등의 목적으로 일회성 토큰을 발행합니다.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "발행 성공",
        content = [
            Content(
                mediaType = "application/json",
                examples = [
                    ExampleObject(
                        name = "success",
                        summary = "일회성 토큰 발행 성공 응답",
                        value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "userId": 123,
                          "purpose": "PASSWORD_RESET",
                          "issuedAt": "2023-12-01T15:30:45.123",
                          "expiresAt": "2023-12-02T15:30:45.123"
                        }
                        """,
                    ),
                ],
            ),
        ],
    )
    @ApiForbiddenError
    @ApiUserNotFoundError
    @PostMapping("v1/tokens/{userId}/one-time-token")
    fun generateOneTimeToken(
        @PathVariable userId: Long,
        @RequestParam purpose: TokenPurpose,
    ): Map<String, Any> {
        logger.debug { "일회성 토큰 발행 요청 - userId=$userId, purpose=$purpose" }

        return userAccountAppService.getUserById(userId).let { user ->
            val token =
                tokenAppService.generateOneTimeToken(
                    email = user.email.value,
                    userId = user.id,
                    purpose = purpose,
                )

            mapOf(
                "token" to token,
                "userId" to user.id,
                "purpose" to purpose,
                "issuedAt" to LocalDateTime.now(),
                "expiresAt" to LocalDateTime.now().plusHours(24),
            )
        }
    }
}
