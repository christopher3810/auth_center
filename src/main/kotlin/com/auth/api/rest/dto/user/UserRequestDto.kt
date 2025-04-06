package com.auth.api.rest.dto.user

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 사용자 회원가입 요청 DTO
 */
@Schema(
    description = "회원가입 요청 객체",
    title = "UserJoinRequest",
    requiredProperties = ["username", "email", "password", "name"],
)
data class UserJoinRequest(
    @Schema(
        description = "사용자 아이디 (로그인에 사용)",
        example = "john_doe",
        minLength = 4,
        maxLength = 20,
        pattern = "^[a-zA-Z0-9_]+$",
        required = true,
    )
    @field:NotBlank(message = "사용자명은 필수 입력값입니다.")
    @field:Size(min = 4, max = 20, message = "사용자명은 4자 이상 20자 이하로 입력해주세요.")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "사용자명은 영문자, 숫자, 언더스코어(_)만 사용 가능합니다.")
    val username: String,
    @Schema(
        description = "사용자 이메일",
        example = "john.doe@example.com",
        format = "email",
        required = true,
    )
    @field:NotBlank(message = "이메일은 필수 입력값입니다.")
    @field:Email(message = "유효한 이메일 형식이 아닙니다.")
    val email: String,
    @Schema(
        description = "사용자 비밀번호",
        example = "SecurePassword123!",
        format = "password",
        minLength = 8,
        pattern = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]+$",
        required = true,
    )
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @field:Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]+$",
        message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다.",
    )
    val password: String,
    @Schema(
        description = "사용자 이름 (실명)",
        example = "John Doe",
        maxLength = 50,
        required = true,
    )
    @field:NotBlank(message = "이름은 필수 입력값입니다.")
    @field:Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    val name: String,
    @Schema(
        description = "사용자 전화번호 (선택 입력, 하이픈 없이 입력)",
        example = "01012345678",
        pattern = "^(01[016789])[0-9]{3,4}[0-9]{4}$",
        required = false,
    )
    @field:Pattern(
        regexp = "^(01[016789])[0-9]{3,4}[0-9]{4}$",
        message = "올바른 전화번호 형식이 아닙니다.",
        flags = [Pattern.Flag.CASE_INSENSITIVE],
    )
    val phoneNumber: String? = null,
)

/**
 * 프로필 수정 요청 DTO
 */
@Schema(
    description = "사용자 프로필 수정 요청 객체",
    title = "UserProfileUpdateRequest",
    requiredProperties = ["name"],
)
data class UserProfileUpdateRequest(
    @Schema(
        description = "변경할 사용자 이름 (실명)",
        example = "John Doe",
        maxLength = 50,
        required = true,
    )
    @field:NotBlank(message = "이름은 필수 입력값입니다.")
    @field:Size(max = 50, message = "이름은 50자 이하로 입력해주세요.")
    val name: String,
    @Schema(
        description = "변경할 사용자 전화번호 (선택 입력, 하이픈 없이 입력)",
        example = "01012345678",
        pattern = "^(01[016789])[0-9]{3,4}[0-9]{4}$",
        required = false,
    )
    @field:Pattern(
        regexp = "^(01[016789])[0-9]{3,4}[0-9]{4}$",
        message = "올바른 전화번호 형식이 아닙니다.",
        flags = [Pattern.Flag.CASE_INSENSITIVE],
    )
    val phoneNumber: String? = null,
)

/**
 * 비밀번호 변경 요청 DTO
 */
@Schema(
    description = "비밀번호 변경 요청 객체",
    title = "UserPasswordChangeRequest",
    requiredProperties = ["currentPassword", "newPassword"],
)
data class UserPasswordChangeRequest(
    @Schema(
        description = "현재 비밀번호",
        example = "CurrentPassword123!",
        format = "password",
        required = true,
    )
    @field:NotBlank(message = "현재 비밀번호는 필수 입력값입니다.")
    val currentPassword: String,
    @Schema(
        description = "새로운 비밀번호",
        example = "NewSecurePassword456!",
        format = "password",
        minLength = 8,
        pattern = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]+$",
        required = true,
    )
    @field:NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    @field:Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]+$",
        message = "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다.",
    )
    val newPassword: String,
)

/**
 * 사용자 역할 변경 요청 DTO
 */
@Schema(
    description = "사용자 역할 변경 요청 객체",
    title = "UserRoleChangeRequest",
    requiredProperties = ["roles"],
)
data class UserRoleChangeRequest(
    @Schema(
        description = "사용자에게 부여할 역할 목록",
        example = "[\"USER\", \"ADMIN\"]",
        required = true,
    )
    @field:NotBlank(message = "역할은 최소 하나 이상 지정해야 합니다.")
    val roles: Set<String>,
)
