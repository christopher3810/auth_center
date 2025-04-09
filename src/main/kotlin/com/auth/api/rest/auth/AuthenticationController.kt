package com.auth.api.rest.auth

import com.auth.api.docs.annotations.ApiAuthError
import com.auth.api.docs.annotations.ApiDuplicateUserError
import com.auth.api.docs.annotations.ApiInvalidTokenError
import com.auth.api.docs.annotations.ApiServerError
import com.auth.api.docs.annotations.ApiUserNotFoundError
import com.auth.api.rest.dto.LoginRequest
import com.auth.api.rest.dto.LogoutRequest
import com.auth.api.rest.dto.user.UserJoinRequest
import com.auth.api.rest.dto.user.UserProfileResponse
import com.auth.api.rest.dto.user.UserRegistrationResponse
import com.auth.application.auth.dto.TokenDto
import com.auth.application.auth.dto.UserTokenInfo
import com.auth.application.auth.service.TokenAppService
import com.auth.application.user.service.UserAccountAppService
import com.auth.exception.InvalidTokenException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

/**
 * 인증(Authentication) 및 세션(로그인/로그아웃/토큰재발급)과 관련된 기능을 제공하는 컨트롤러.
 */
@Tag(
    name = "Authentication",
    description = "인증 및 사용자 관리 API - 회원가입, 로그인, 토큰 관리, 사용자 정보 조회 등의 기능을 제공합니다.",
)
@RestController
@RequestMapping("/api/users")
class AuthenticationController(
    private val userAccountAppService: UserAccountAppService,
    private val tokenAppService: TokenAppService,
) {
    @Operation(
        summary = "로그인",
        description = """
            이메일 또는 사용자명을 이용하여 로그인하고, 액세스 토큰과
            리프레시 토큰을 발급받습니다.
        """,
    )
    @ApiResponse(
        responseCode = "200",
        description = "로그인 성공",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = TokenDto::class),
                examples = [
                    ExampleObject(
                        name = "success",
                        summary = "정상 로그인 응답",
                        description = "액세스 토큰과 리프레시 토큰이 포함된 응답입니다.",
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
    @ApiUserNotFoundError
    @ApiServerError
    @PostMapping("v1/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
    ): TokenDto {
        logger.debug { "로그인 요청: $loginRequest" }

        return userAccountAppService
            .validateLogin(
                usernameOrEmail = loginRequest.usernameOrEmail,
                rawPassword = loginRequest.password,
            ).let { user ->
                userAccountAppService.recordUserLogin(user.id)
                tokenAppService.generateTokens(user)
            }
    }

    @Operation(
        summary = "로그아웃",
        description = """
            서버에 저장된 리프레시 토큰을 무효화하여 로그아웃 처리합니다.
        """,
    )
    @ApiResponse(responseCode = "200", description = "로그아웃 성공 (토큰 무효화 성공)")
    @ApiUserNotFoundError
    @ApiInvalidTokenError
    @ApiServerError
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
    ) {
        logger.debug { "로그아웃 요청 - 리프레시 토큰 무효화: ${request.refreshToken}" }

        tokenAppService.revokeRefreshToken(request.refreshToken).let { revoked ->
            if (!revoked) {
                throw InvalidTokenException("이미 무효화되었거나, 해당 리프레시 토큰이 존재하지 않습니다.")
            }
        }
    }

    @Operation(
        summary = "회원가입",
        description = """
            새로운 사용자를 등록하고, 등록 결과(회원가입 완료 메시지)를 반환합니다.
        """,
    )
    @ApiResponse(
        responseCode = "201",
        description = "회원가입 성공",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = UserRegistrationResponse::class),
                examples = [
                    ExampleObject(
                        name = "success",
                        summary = "회원가입 성공 응답",
                        value = """
                        {
                          "id": 123,
                          "username": "john_doe",
                          "email": "john.doe@example.com",
                          "message": "회원가입이 완료되었습니다."
                        }
                        """,
                    ),
                ],
            ),
        ],
    )
    @ApiDuplicateUserError
    @ApiServerError
    @PostMapping("v1/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(
        @Valid @RequestBody request: UserJoinRequest,
    ): UserRegistrationResponse {
        logger.debug { "회원가입 요청: $request" }

        return userAccountAppService
            .registerUser(
                username = request.username,
                email = request.email,
                password = request.password,
                name = request.name,
                phoneNumber = request.phoneNumber,
            ).let(UserRegistrationResponse::from)
    }

    @Operation(
        summary = "내 정보 조회",
        description = """
            현재 인증된 사용자의 프로필 정보를 조회합니다.
        """,
    )
    @ApiAuthError
    @ApiUserNotFoundError
    @ApiServerError
    @GetMapping("v1/me")
    fun getMyProfile(
        @AuthenticationPrincipal userInfo: UserTokenInfo,
    ): UserProfileResponse {
        logger.debug { "내 정보 조회 요청" }

        return userAccountAppService
            .getUserById(userInfo.id)
            .let(UserProfileResponse::from)
    }
}
