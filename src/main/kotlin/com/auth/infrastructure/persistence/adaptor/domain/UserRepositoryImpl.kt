package com.auth.infrastructure.persistence.adaptor

import com.auth.domain.user.entity.UserEntity
import com.auth.domain.user.repository.UserRepository
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.UserStatus
import com.auth.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
) : UserRepository {
    
    override fun findByEmail(email: Email): Optional<UserEntity> {
        return jpaRepository.findByEmail(email)
    }

    override fun findByUsername(username: String): Optional<UserEntity> {
        return jpaRepository.findByUserName(username)
    }

    override fun existsByEmail(email: Email): Boolean {
        return jpaRepository.existsByEmail(email)
    }

    override fun existsByUsername(username: String): Boolean {
        return jpaRepository.existsByUserName(username)
    }

    override fun findById(id: Long): Optional<UserEntity> {
        return jpaRepository.findById(id)
    }

    override fun findAllByStatus(status: UserStatus): List<UserEntity> {
        return jpaRepository.findAllByStatus(status)
    }

    override fun save(userEntity: UserEntity): UserEntity {
        return jpaRepository.save(userEntity)
    }
    
    override fun delete(user: UserEntity) {
        jpaRepository.delete(user)
    }
    
    override fun findAll(): List<UserEntity> {
        return jpaRepository.findAll()
    }
} 