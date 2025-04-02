package com.auth.domain.user.event

import com.auth.domain.user.model.User
import com.auth.domain.user.value.UserStatus
import java.time.LocalDateTime

/**
 * 사용자 도메인 이벤트 추상 클래스
 */
sealed class UserEvent(
    open val user: User,
    open val timestamp: LocalDateTime = LocalDateTime.now(),
)

/**
 * 사용자 생성 이벤트
 */
data class UserCreatedEvent(
    override val user: User,
) : UserEvent(user)

/**
 * 사용자 상태 변경 이벤트
 */
data class UserStatusChangedEvent(
    override val user: User,
    val oldStatus: UserStatus,
    val newStatus: UserStatus,
) : UserEvent(user)

/**
 * 사용자 비밀번호 변경 이벤트
 */
data class UserPasswordChangedEvent(
    override val user: User,
) : UserEvent(user)

/**
 * 사용자 로그인 이벤트
 */
data class UserLoggedInEvent(
    override val user: User,
) : UserEvent(user)

/**
 * 사용자 역할 변경 이벤트
 */
data class UserRolesChangedEvent(
    override val user: User,
    val oldRoles: Set<String>,
    val newRoles: Set<String>,
) : UserEvent(user)

/**
 * 사용자 삭제 이벤트
 */
data class UserDeletedEvent(
    override val user: User,
) : UserEvent(user)
