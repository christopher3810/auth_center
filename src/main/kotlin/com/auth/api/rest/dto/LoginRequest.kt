package com.auth.api.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

/**
 * 로그인 요청 DTO
 * 사용자 인증을 위한 요청 데이터를 담습니다.
 */
@Schema(
    description = "로그인 요청 객체",
    title = "LoginRequest",
    requiredProperties = ["usernameOrEmail", "password"],
)
data class LoginRequest(
    @Schema(
        description = "로그인에 사용할 이메일 또는 사용자명",
        example = "john_doe 또는 john.doe@example.com",
        minLength = 3,
        maxLength = 255,
        required = true,
    )
    @field:NotBlank(message = "아이디(혹은 이메일)는 필수입니다.")
    val usernameOrEmail: String,
    @Schema(
        description = "사용자 비밀번호",
        example = "SecurePassword123!",
        format = "password",
        minLength = 8,
        required = true,
    )
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,
)
