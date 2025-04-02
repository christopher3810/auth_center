package com.auth.infrastructure.repository

import com.auth.domain.user.entity.UserEntity
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    /**
     * 이메일로 사용자 찾기
     */
    fun findByEmail(email: Email): UserEntity?

    /**
     * 사용자 이름으로 사용자 찾기
     */
    fun findByUserName(userName: String): UserEntity?

    /**
     * 사용자 상태로 사용자 찾기
     */
    fun findAllByStatus(userStatus: UserStatus): List<UserEntity>

    /**
     * 이메일 존재 여부 확인
     */
    fun existsByEmail(email: Email): Boolean

    /**
     * 사용자 이름 존재 여부 확인
     */
    fun existsByUserName(userName: String): Boolean
}
