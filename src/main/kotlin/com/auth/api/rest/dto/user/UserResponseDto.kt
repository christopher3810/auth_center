package com.auth.api.rest.dto.user

import com.auth.domain.user.model.User
import com.fasterxml.jackson.annotation.JsonInclude
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
    val updatedAt: LocalDateTime?
) {
    companion object {
        /**
         * 도메인 모델을 응답 DTO로 변환
         */
        fun from(user: User): UserDetailResponse {
            return UserDetailResponse(
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
                updatedAt = user.updatedAt
            )
        }
        
        /**
         * nullable한 도메인 모델을 응답 DTO로 변환
         */
        fun fromNullable(user: User?): UserDetailResponse? {
            return user?.let { from(it) }
        }
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
    val lastLoginAt: LocalDateTime?
) {
    companion object {
        /**
         * 도메인 모델을 간소화된 응답 DTO로 변환
         */
        fun from(user: User): UserSummaryResponse {
            return UserSummaryResponse(
                id = user.id,
                username = user.username,
                email = user.email.value,
                name = user.name,
                status = user.status.name,
                isActive = user.isActive(),
                lastLoginAt = user.lastLoginAt
            )
        }
        
        /**
         * 도메인 모델 목록을 응답 DTO 목록으로 변환
         */
        fun from(users: List<User>): List<UserSummaryResponse> {
            return users.map { from(it) }
        }
        
        /**
         * nullable한 도메인 모델을 응답 DTO로 변환
         */
        fun fromNullable(user: User?): UserSummaryResponse? {
            return user?.let { from(it) }
        }
    }
}

/**
 * 사용자 프로필 응답 DTO (개인정보 노출 최소화)
 */
data class UserProfileResponse(
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
     * 마지막 로그인 일시
     */
    val lastLoginAt: LocalDateTime?
) {
    companion object {
        /**
         * 도메인 모델을 프로필 응답 DTO로 변환
         */
        fun from(user: User): UserProfileResponse {
            return UserProfileResponse(
                username = user.username,
                email = user.email.value,
                name = user.name,
                phoneNumber = user.phoneNumber,
                roles = user.roles,
                lastLoginAt = user.lastLoginAt
            )
        }
        
        /**
         * nullable한 도메인 모델을 응답 DTO로 변환
         */
        fun fromNullable(user: User?): UserProfileResponse? {
            return user?.let { from(it) }
        }
    }
}

/**
 * 사용자 등록 결과 응답 DTO
 */
data class UserRegistrationResponse(
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
     * 메시지
     */
    val message: String = "사용자 등록이 완료되었습니다. 이메일 인증을 통해 계정을 활성화해주세요."
) {
    companion object {
        /**
         * 도메인 모델을 등록 결과 응답 DTO로 변환
         */
        fun from(user: User): UserRegistrationResponse {
            return UserRegistrationResponse(
                id = user.id,
                username = user.username,
                email = user.email.value
            )
        }
    }
} 