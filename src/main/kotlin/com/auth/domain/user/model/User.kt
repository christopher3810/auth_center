package com.auth.domain.user.model

import com.auth.domain.user.event.*
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.Password
import com.auth.domain.user.value.UserStatus
import java.time.LocalDateTime

/**
 * 사용자 도메인 모델 아이덴티티의 중심이 되는 도메인 엔티티.
 * LifeCycle : 사용자는 등록 → 활성화 → 사용 → (필요 시 잠금) → 비활성화/삭제 단계를 거침"
 */
class User(
    val id: Long = 0,
    val username: String,
    val email: Email,
    var password: Password,
    var name: String,
    var phoneNumber: String? = null,
    var roles: Set<String> = setOf(),
    var status: UserStatus = UserStatus.INACTIVE,
    var lastLoginAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null
) {

    private val domainEvents = mutableListOf<UserEvent>()

    /**
     * 사용자 활성화
     */
    fun activate(): User {
        if (status != UserStatus.ACTIVE) {
            val oldStatus = status
            status = UserStatus.ACTIVE
            registerEvent(UserStatusChangedEvent(this, oldStatus, status))
        }
        return this
    }
    
    /**
     * 사용자 비활성화
     */
    fun deactivate(): User {
        if (status != UserStatus.INACTIVE) {
            val oldStatus = status
            status = UserStatus.INACTIVE
            registerEvent(UserStatusChangedEvent(this, oldStatus, status))
        }
        return this
    }
    
    /**
     * 사용자 계정 잠금
     */
    fun lock(): User {
        if (status != UserStatus.LOCKED) {
            val oldStatus = status
            status = UserStatus.LOCKED
            registerEvent(UserStatusChangedEvent(this, oldStatus, status))
        }
        return this
    }
    
    /**
     * 사용자 휴면 상태로 변경
     */
    fun markAsDormant(): User {
        if (status != UserStatus.DORMANT) {
            val oldStatus = status
            status = UserStatus.DORMANT
            registerEvent(UserStatusChangedEvent(this, oldStatus, status))
        }
        return this
    }
    
    /**
     * 사용자 비밀번호 변경
     */
    fun changePassword(newPassword: Password): User {
        password = newPassword
        registerEvent(UserPasswordChangedEvent(this))
        return this
    }
    
    /**
     * 사용자 로그인 처리
     */
    fun recordLogin(): User {
        lastLoginAt = LocalDateTime.now()
        registerEvent(UserLoggedInEvent(this))
        return this
    }
    
    /**
     * 사용자에게 역할 추가
     */
    fun addRole(role: String): User {
        val oldRoles = roles.toSet()
        roles = roles + role
        registerEvent(UserRolesChangedEvent(this, oldRoles, roles))
        return this
    }
    
    /**
     * 사용자에게서 역할 제거
     */
    fun removeRole(role: String): User {
        if (role in roles) {
            val oldRoles = roles.toSet()
            roles = roles - role
            registerEvent(UserRolesChangedEvent(this, oldRoles, roles))
        }
        return this
    }
    
    /**
     * 사용자 역할 일괄 변경
     */
    fun updateRoles(newRoles: Set<String>): User {
        if (newRoles != roles) {
            val oldRoles = roles.toSet()
            roles = newRoles
            registerEvent(UserRolesChangedEvent(this, oldRoles, newRoles))
        }
        return this
    }
    
    /**
     * 사용자가 특정 역할을 가지고 있는지 확인
     */
    fun hasRole(role: String): Boolean {
        return role in roles
    }
    
    /**
     * 사용자 정보 업데이트
     */
    fun update(name: String? = null, phoneNumber: String? = null): User {
        name?.let { this.name = it }
        phoneNumber?.let { this.phoneNumber = it }
        updatedAt = LocalDateTime.now()
        return this
    }
    
    /**
     * 사용자가 활성 상태인지 확인
     */
    fun isActive(): Boolean {
        return status == UserStatus.ACTIVE
    }
    
    /**
     * 로그인 가능 상태인지 확인
     */
    fun isLoginable(): Boolean {
        return status.isUsable()
    }
    
    /**
     * 이벤트 등록
     */
    fun registerEvent(event: UserEvent) {
        domainEvents.add(event)
    }
    
    /**
     * 도메인 이벤트 조회 및 소비
     */
    fun consumeEvents(): List<UserEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }
    
    companion object {
        /**
         * 새 사용자 생성 팩토리 메소드
         * 모델링 문서 참고: "Self-Service 등록 시에는 미인증 상태로 생성되고 이메일 검증을 거쳐 활성화됩니다."
         */
        fun create(
            username: String,
            email: Email,
            password: Password,
            name: String,
            phoneNumber: String? = null,
            initialRoles: Set<String> = setOf()
        ): User {
            val user = User(
                username = username,
                email = email,
                password = password,
                name = name,
                phoneNumber = phoneNumber,
                roles = initialRoles,
                status = UserStatus.INACTIVE,
                createdAt = LocalDateTime.now()
            )
            user.registerEvent(UserCreatedEvent(user))
            return user
        }
        
        /**
         * 관리자에 의한 사용자 생성 팩토리 메소드
         * 모델링 문서 참고: "관리자가 생성하는 경우 바로 활성화할 수 있습니다."
         */
        fun createByAdmin(
            username: String,
            email: Email,
            password: Password,
            name: String,
            phoneNumber: String? = null,
            initialRoles: Set<String> = setOf()
        ): User {
            val user = User(
                username = username,
                email = email,
                password = password,
                name = name,
                phoneNumber = phoneNumber,
                roles = initialRoles,
                status = UserStatus.ACTIVE,
                createdAt = LocalDateTime.now()
            )
            user.registerEvent(UserCreatedEvent(user))
            return user
        }
    }
} 