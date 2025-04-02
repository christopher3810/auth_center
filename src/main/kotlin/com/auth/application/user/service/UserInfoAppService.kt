package com.auth.application.user.service

import com.auth.domain.user.model.User
import com.auth.domain.user.service.UserDomainService
import com.auth.domain.user.value.Email
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserInfoAppService(
    private val userDomainService: UserDomainService,
) {
    /**
     * 사용자 ID로 조회
     */
    @Transactional(readOnly = true)
    fun getUserById(userId: Long): User = userDomainService.findUserById(userId)

    /**
     * 이메일로 사용자 조회
     */
    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): User = userDomainService.findUserByEmail(Email(email))

    /**
     * 사용자명으로 사용자 조회
     */
    @Transactional(readOnly = true)
    fun getUserByUsername(username: String): User = userDomainService.findUserByUsername(username)

    /**
     * 전체 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    fun getAllUsers(): List<User> = userDomainService.findAllUsers()
}
