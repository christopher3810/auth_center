package com.auth.domain.user.service

import com.auth.domain.user.entity.UserEntity
import com.auth.domain.user.factory.UserFactory
import com.auth.domain.user.model.User
import com.auth.domain.user.repository.UserRepository
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.UserStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

/**
 * 사용자 도메인 서비스
 * 도메인 모델에 비즈니스 로직 적용
 * 이렇게 조회와 생성 책임을 명확히 분리합니다.
 */
@Service
class UserDomainService(
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun findUserById(id: Long): Optional<User> {
        val userEntityOpt = userRepository.findById(id)
        return userEntityOpt.map { UserFactory.createFromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun findUserByEmail(email: Email): Optional<User> {
        val userEntityOpt = userRepository.findByEmail(email)
        return userEntityOpt.map { UserFactory.createFromEntity(it) }
    }

    @Transactional(readOnly = true)
    fun findUserByUsername(username: String): Optional<User> {
        val userEntityOpt = userRepository.findByUsername(username)
        return userEntityOpt.map { UserFactory.createFromEntity(it) }
    }
    
    /**
     * 상태별 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    fun findUsersByStatus(status: UserStatus): List<User> {
        val userEntities = userRepository.findAllByStatus(status)
        return userEntities.map { UserFactory.createFromEntity(it) }
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

        if (userRepository.existsByEmail(emailObj)) {
            throw IllegalArgumentException("이미 사용 중인 이메일입니다: $email")
        }
        
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("이미 사용 중인 사용자명입니다: $username")
        }

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
            val existingEntity = userRepository.findById(user.id)
                .orElseThrow { IllegalArgumentException("존재하지 않는 사용자 ID: ${user.id}") }

            // Entity 업데이트
            UserFactory.updateEntity(existingEntity, user)
        }
        
        // 2. Entity 저장
        val savedEntity = userRepository.save(userEntity)
        
        // 3. 저장된 Entity -> 도메인 모델 변환 (Factory 사용)
        return UserFactory.createFromEntity(savedEntity)
    }
    
    /**
     * 사용자 삭제
     */
    @Transactional
    fun deleteUser(user: User) {
        userRepository.findById(user.id).ifPresent { entity ->
            userRepository.delete(entity)
        }
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