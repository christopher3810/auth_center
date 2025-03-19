package com.auth.domain.user.service

import com.auth.domain.user.entity.UserEntity
import com.auth.domain.user.factory.UserFactory
import com.auth.domain.user.model.User
import com.auth.domain.user.repository.UserRepository
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.UserStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


inline fun <T> T?.orThrow(exceptionProvider: () -> Throwable): T {
    return this ?: throw exceptionProvider()
}


/**
 * 사용자 도메인 서비스
 */
@Service
class UserDomainService(
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun findUserById(id: Long): User? {
        val entity = userRepository.findById(id)
            .orThrow { NoSuchElementException("사용자를 찾을 수 없습니다 : userId - $id") }
        return UserFactory.createFromEntity(entity)
    }

    @Transactional(readOnly = true)
    fun findUserByEmail(email: Email): User? {
        val entity = userRepository.findByEmail(email)
            .orThrow { NoSuchElementException("사용자를 찾을 수 없습니다 : email - $email") }
        return UserFactory.createFromEntity(entity)
    }

    @Transactional(readOnly = true)
    fun findUserByUsername(username: String): User? {
        val entity = userRepository.findByUsername(username)
            .orThrow { NoSuchElementException("사용자를 찾을 수 없습니다 : username - $username") }
        return UserFactory.createFromEntity(entity)
    }

    /**
     * 상태별 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    fun findUsersByStatus(status: UserStatus): List<User> {
        return userRepository.findAllByStatus(status)
            .map { UserFactory.createFromEntity(it) }
    }
    
    /**
     * 사용자 생성 - 일반 회원가입
     */
    @Transactional
    fun createUser(
        username: String,
        email: String,
        rawPassword: String,
        name: String,
        phoneNumber: String? = null
    ): User {

        val emailObj = Email(email)

        require(!userRepository.existsByEmail(emailObj)) { "이미 사용 중인 이메일입니다: $email" }
        require(!userRepository.existsByUsername(username)) { "이미 사용 중인 사용자명입니다: $username" }

        val user = UserFactory.createUser(
            username = username,
            email = email,
            rawPassword = rawPassword,
            name = name,
            phoneNumber = phoneNumber
        )

        val userEntity = UserFactory.createEntity(user)

        val savedEntity = userRepository.save(userEntity)

        return UserFactory.createFromEntity(savedEntity)
    }
    
    /**
     * 사용자 모델 저장
     */
    @Transactional
    fun saveUser(user: User): User {

        val userEntity: UserEntity = if (user.id == 0L) {
            // 신규 사용자인 경우
            UserFactory.createEntity(user)
        } else {
            // 기존 사용자 업데이트인 경우
            userRepository.findById(user.id)
                ?.let { existingEntity -> UserFactory.updateEntity(existingEntity, user)
            } ?: throw IllegalArgumentException("존재하지 않는 사용자 ID: ${user.id}")
        }

        // 2. Entity 저장
        val savedEntity = userRepository.save(userEntity)

        return UserFactory.createFromEntity(savedEntity)
    }
    
    /**
     * 사용자 삭제
     */
    @Transactional
    fun deleteUser(user: User) {
        val entity = userRepository.findById(user.id)
            .orThrow { NoSuchElementException("사용자를 찾을 수 없습니다 : userId - $user.id") }
        userRepository.delete(entity)
    }
    
    /**
     * 모든 사용자 조회
     */
    @Transactional(readOnly = true)
    fun findAllUsers(): List<User> {
        val userEntities = userRepository.findAll()
        return userEntities.map { UserFactory.createFromEntity(it) }
    }
} 