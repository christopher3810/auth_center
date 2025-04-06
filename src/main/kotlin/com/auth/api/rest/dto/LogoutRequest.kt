package com.auth.api.rest.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

/**
 * 로그아웃 요청 DTO
 * 로그아웃 시 무효화할 토큰 정보를 담습니다.
 */
@Schema(
    description = "로그아웃 요청 객체 - 유효한 리프레시 토큰 정보를 전송해야 합니다.",
    title = "LogoutRequest",
    requiredProperties = ["refreshToken"],
)
data class LogoutRequest(
    @Schema(
        description = "무효화할 리프레시 토큰 값",
        example = "dklajdslkfjaldkjfalkdsjflaksdjflkadsdjfaldskjfasdlkfjalkdsfjalksdjfasdlkjf",
        required = true,
        format = "jwt",
    )
    @field:NotBlank(message = "리프레시 토큰은 필수입니다.")
    val refreshToken: String,
)
