package com.auth.api.rest.auth

import com.auth.api.rest.dto.TokenRefreshRequest
import com.auth.application.auth.dto.TokenDto
import com.auth.application.auth.service.TokenAppService
import com.auth.application.user.service.UserAccountAppService
import com.auth.domain.auth.model.TokenPurpose
import com.auth.domain.user.model.User
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * 인가(Authorization) 및 추가 인증 로직을 처리하는 컨트롤러
 * - 계정 상태 변경(activate, deactivate, lock)
 * - 일회성 토큰 발행 (이메일 인증 등)
 */
@Tag(name = "Users", description = "사용자 프로필 및 계정 관리 API")
@RestController
@RequestMapping("/api/auth")
class AuthorizationController(
    private val userAccountAppService: UserAccountAppService,
    private val tokenAppService: TokenAppService,
) {
    @Operation(
        summary = "액세스 토큰 재발급",
        description = "유효한 리프레시 토큰을 보내 새 액세스 토큰과 리프레시 토큰을 발급받습니다. 토큰 교체(Token Rotation) 패턴을 적용하여 보안을 강화합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰 (만료, 이미 사용됨, 형식 오류)"),
            ApiResponse(responseCode = "404", description = "시스템에 존재하지 않는 리프레시 토큰"),
            ApiResponse(responseCode = "403", description = "비활성화된 사용자 계정"),
        ],
    )
    @PostMapping("v1/token/refresh")
    fun refreshToken(
        @Valid @RequestBody request: TokenRefreshRequest,
    ): TokenDto {
        logger.debug { "토큰 재발급 요청: ${request.refreshToken.take(10)}..." }
        return tokenAppService.refreshAccessToken(request.refreshToken)
    }

    /**
     * 이메일 인증용 혹은 비밀번호 재설정용 등으로 쓰이는 일회성 토큰 발행 예시
     *
     * @param userId  대상 사용자 식별자
     * @param purpose 토큰 목적 (EMAIL_VERIFICATION, PASSWORD_RESET 등)
     */
    @Operation(summary = "일회성 토큰 발행", description = "이메일 인증·비밀번호 재설정 등에 사용할 일회성 토큰 발행")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "발행 성공"),
            ApiResponse(responseCode = "404", description = "대상 사용자가 존재하지 않음"),
        ],
    )
    @PostMapping("v1/tokens/one-time-token")
    fun generateOneTimeToken(
        @PathVariable userId: Long,
        @RequestParam purpose: TokenPurpose,
    ): Map<String, Any> {
        logger.debug { "일회성 토큰 발행 요청 - userId=$userId, purpose=$purpose" }

        val user: User = userAccountAppService.getUserById(userId)

        val token =
            tokenAppService.generateOneTimeToken(
                email = user.email.value,
                userId = user.id,
                purpose = purpose,
            )

        return mapOf(
            "token" to token,
            "userId" to user.id,
            "purpose" to purpose,
            "issuedAt" to LocalDateTime.now(),
        )
    }

    @Operation(summary = "계정 활성화", description = "주어진 userId의 계정을 활성화합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "활성화 성공 (이미 활성화 되어 있었다면 false 반환)"),
            ApiResponse(responseCode = "404", description = "해당 사용자가 존재하지 않음"),
        ],
    )
    @PatchMapping("v1/users/{userId}/activate")
    fun activateUser(
        @PathVariable userId: Long,
    ): Boolean = userAccountAppService.activateUser(userId)

    @Operation(summary = "계정 비활성화", description = "주어진 userId의 계정을 비활성화합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "비활성화 성공 (이미 비활성화 되어 있었다면 false 반환)"),
            ApiResponse(responseCode = "404", description = "해당 사용자가 존재하지 않음"),
        ],
    )
    @PatchMapping("v1/users/{userId}/deactivate")
    fun deactivateUser(
        @PathVariable userId: Long,
    ): Boolean = userAccountAppService.deactivateUser(userId)

    @Operation(summary = "계정 잠금", description = "주어진 userId의 계정을 잠금 상태로 전환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "잠금 성공"),
            ApiResponse(responseCode = "404", description = "해당 사용자가 존재하지 않음"),
        ],
    )
    @PatchMapping("v1/users/{userId}/lock")
    fun lockUser(
        @PathVariable userId: Long,
    ): Boolean {
        return userAccountAppService.lockUser(userId).let { true } // lockUser()는 무조건 true 반환한다고 가정
    }
}
