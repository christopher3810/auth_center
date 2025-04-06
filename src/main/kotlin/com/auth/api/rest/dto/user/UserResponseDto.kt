package com.auth.api.rest.dto.user

import com.auth.domain.user.model.User
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * 사용자 정보 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDetailResponse(
    /**
     * 사용자 식별자
     */
    val id: Long,
    /**
     * 사용자명 (로그인 아이디)
     */
    val username: String,
    /**
     * 사용자 이메일
     */
    val email: String,
    /**
     * 사용자 이름
     */
    val name: String,
    /**
     * 사용자 전화번호
     */
    val phoneNumber: String?,
    /**
     * 사용자 역할 목록
     */
    val roles: Set<String>,
    /**
     * 사용자 상태
     */
    val status: String,
    /**
     * 활성화 여부
     */
    val isActive: Boolean,
    /**
     * 마지막 로그인 일시
     */
    val lastLoginAt: LocalDateTime?,
    /**
     * 가입 일시
     */
    val createdAt: LocalDateTime?,
    /**
     * 정보 수정 일시
     */
    val updatedAt: LocalDateTime?,
) {
    companion object {
        /**
         * 도메인 모델을 응답 DTO로 변환
         */
        fun from(user: User): UserDetailResponse =
            UserDetailResponse(
                id = user.id,
                username = user.username,
                email = user.email.value,
                name = user.name,
                phoneNumber = user.phoneNumber,
                roles = user.roles,
                status = user.status.name,
                isActive = user.isActive(),
                lastLoginAt = user.lastLoginAt,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
            )

        /**
         * nullable한 도메인 모델을 응답 DTO로 변환
         */
        fun fromNullable(user: User?): UserDetailResponse? = user?.let { from(it) }
    }
}

/**
 * 사용자 목록 항목 응답 DTO (간소화된 정보)
 */
data class UserSummaryResponse(
    /**
     * 사용자 식별자
     */
    val id: Long,
    /**
     * 사용자명 (로그인 아이디)
     */
    val username: String,
    /**
     * 사용자 이메일
     */
    val email: String,
    /**
     * 사용자 이름
     */
    val name: String,
    /**
     * 사용자 상태
     */
    val status: String,
    /**
     * 활성화 여부
     */
    val isActive: Boolean,
    /**
     * 마지막 로그인 일시
     */
    val lastLoginAt: LocalDateTime?,
) {
    companion object {
        /**
         * 도메인 모델을 간소화된 응답 DTO로 변환
         */
        fun from(user: User): UserSummaryResponse =
            UserSummaryResponse(
                id = user.id,
                username = user.username,
                email = user.email.value,
                name = user.name,
                status = user.status.name,
                isActive = user.isActive(),
                lastLoginAt = user.lastLoginAt,
            )

        /**
         * 도메인 모델 목록을 응답 DTO 목록으로 변환
         */
        fun from(users: List<User>): List<UserSummaryResponse> = users.map { from(it) }

        /**
         * nullable한 도메인 모델을 응답 DTO로 변환
         */
        fun fromNullable(user: User?): UserSummaryResponse? = user?.let { from(it) }
    }
}

/**
 * 사용자 프로필 응답 DTO (개인정보 노출 최소화)
 */
@Schema(
    description = "사용자 프로필 정보 응답 객체",
    title = "UserProfileResponse",
)
data class UserProfileResponse(
    @Schema(
        description = "사용자명 (로그인 아이디)",
        example = "john_doe",
    )
    val username: String,
    @Schema(
        description = "사용자 이메일",
        example = "john.doe@example.com",
        format = "email",
    )
    val email: String,
    @Schema(
        description = "사용자 이름",
        example = "John Doe",
    )
    val name: String,
    @Schema(
        description = "사용자 전화번호",
        example = "01012345678",
        nullable = true,
    )
    val phoneNumber: String?,
    @Schema(
        description = "사용자 역할 목록",
        example = "[\"USER\", \"ADMIN\"]",
    )
    val roles: Set<String>,
    @Schema(
        description = "마지막 로그인 일시",
        example = "2023-05-15T14:30:15",
        format = "date-time",
        nullable = true,
    )
    val lastLoginAt: LocalDateTime?,
) {
    companion object {
        /**
         * 도메인 모델을 프로필 응답 DTO로 변환
         */
        fun from(user: User): UserProfileResponse =
            UserProfileResponse(
                username = user.username,
                email = user.email.value,
                name = user.name,
                phoneNumber = user.phoneNumber,
                roles = user.roles,
                lastLoginAt = user.lastLoginAt,
            )

        /**
         * nullable한 도메인 모델을 응답 DTO로 변환
         */
        fun fromNullable(user: User?): UserProfileResponse? = user?.let { from(it) }
    }
}

/**
 * 사용자 등록 결과 응답 DTO
 */
@Schema(
    description = "회원가입 성공 응답 객체",
    title = "UserRegistrationResponse",
)
data class UserRegistrationResponse(
    @Schema(
        description = "사용자 식별자",
        example = "123",
    )
    val id: Long,
    @Schema(
        description = "사용자명 (로그인 아이디)",
        example = "john_doe",
    )
    val username: String,
    @Schema(
        description = "사용자 이메일",
        example = "john.doe@example.com",
        format = "email",
    )
    val email: String,
    @Schema(
        description = "회원가입 완료 메시지",
        example = "사용자 등록이 완료되었습니다. 이메일 인증을 통해 계정을 활성화해주세요.",
    )
    val message: String = "사용자 등록이 완료되었습니다. 이메일 인증을 통해 계정을 활성화해주세요.",
) {
    companion object {
        /**
         * 도메인 모델을 등록 결과 응답 DTO로 변환
         */
        fun from(user: User): UserRegistrationResponse =
            UserRegistrationResponse(
                id = user.id,
                username = user.username,
                email = user.email.value,
            )
    }
}
