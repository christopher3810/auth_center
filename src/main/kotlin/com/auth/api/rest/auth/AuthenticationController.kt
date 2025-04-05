package com.auth.api.rest.auth

import com.auth.api.rest.dto.LoginRequest
import com.auth.api.rest.dto.LogoutRequest
import com.auth.api.rest.dto.user.UserJoinRequest
import com.auth.api.rest.dto.user.UserProfileResponse
import com.auth.api.rest.dto.user.UserRegistrationResponse
import com.auth.application.auth.dto.TokenDto
import com.auth.application.auth.service.TokenAppService
import com.auth.application.user.service.UserAccountAppService
import com.auth.domain.user.model.User
import com.auth.exception.InvalidAuthorizationHeaderException
import com.auth.exception.InvalidTokenException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
@Tag(name = "Authentication", description = "회원가입, 로그인, 토큰 재발급, 로그아웃 등의 인증 및 세션 관리")
@RestController
@RequestMapping("/api/users")
class AuthenticationController(
    private val userAccountAppService: UserAccountAppService,
    private val tokenAppService: TokenAppService,
) {
    @Operation(
        summary = "로그인",
        description = "이메일 또는 사용자명을 이용하여 로그인하고, 액세스 토큰과 리프레시 토큰을 발급받습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "401", description = "인증 실패 (아이디/비밀번호 불일치)"),
        ],
    )
    @PostMapping("v1/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
    ): TokenDto {
        logger.debug { "로그인 요청: $loginRequest" }

        val user =
            userAccountAppService.validateLogin(
                usernameOrEmail = loginRequest.usernameOrEmail,
                rawPassword = loginRequest.password,
            )
        userAccountAppService.recordUserLogin(user.id)
        return tokenAppService.generateTokens(user)
    }

    @Operation(
        summary = "로그아웃",
        description = "서버에 저장된 리프레시 토큰을 무효화하여 로그아웃 처리합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그아웃 성공 (토큰 무효화 성공)"),
            ApiResponse(responseCode = "404", description = "이미 무효화되었거나 존재하지 않는 토큰"),
        ],
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
    ) {
        logger.debug { "로그아웃 요청 - 리프레시 토큰 무효화: ${request.refreshToken}" }
        val revoked = tokenAppService.revokeRefreshToken(request.refreshToken)
        if (!revoked) {
            throw InvalidTokenException("이미 무효화되었거나, 해당 리프레시 토큰이 존재하지 않습니다.")
        }
    }

    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록하고, 등록 결과(회원가입 완료 메시지)를 반환합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "회원가입 성공"),
            ApiResponse(responseCode = "400", description = "유효성 문제 혹은 이미 존재하는 사용자"),
            ApiResponse(responseCode = "500", description = "서버 내부 오류"),
        ],
    )
    @PostMapping("v1/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(
        @Valid @RequestBody request: UserJoinRequest,
    ): UserRegistrationResponse {
        logger.debug { "회원가입 요청: $request" }

        val user: User =
            userAccountAppService.registerUser(
                username = request.username,
                email = request.email,
                password = request.password,
                name = request.name,
                phoneNumber = request.phoneNumber,
            )

        return UserRegistrationResponse.from(user)
    }

    /**
     * 내 정보 조회
     *
     * @param request - 액세스 토큰 (Bearer)이 포함된 HttpServletRequest
     * @return UserProfileResponse
     */
    @Operation(summary = "내 정보 조회", description = "Bearer 액세스 토큰이 필요한 사용자 프로필 조회 API")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "토큰 누락 or 유효하지 않음"),
            ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음"),
        ],
    )
    @GetMapping("v1/info")
    fun getMyProfile(request: HttpServletRequest): UserProfileResponse {
        logger.debug { "내 정보 조회 요청" }

        val authHeader =
            request.getHeader("Authorization")
                ?: throw InvalidAuthorizationHeaderException("Authorization 헤더 누락")

        val tokenValue = authHeader.substring("Bearer ".length)

        // 액세스 토큰 검증
        if (!tokenAppService.validateToken(tokenValue)) {
            throw InvalidTokenException("액세스 토큰이 만료되었거나 유효하지 않습니다.")
        }

        // 토큰에서 사용자 정보 추출
        val userInfo = tokenAppService.getUserInfoFromToken(tokenValue)

        val user = userAccountAppService.getUserById(userInfo.id)

        // 프로필 변환
        return UserProfileResponse.from(user)
    }
}
