package com.auth.infrastructure.persistence.adaptor

import com.auth.domain.user.entity.UserEntity
import com.auth.domain.user.repository.UserRepository
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.UserStatus
import com.auth.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository,
) : UserRepository {
    override fun findByEmail(email: Email): UserEntity? = jpaRepository.findByEmail(email)

    override fun findByUsername(username: String): UserEntity? = jpaRepository.findByUserName(username)

    override fun existsByEmail(email: Email): Boolean = jpaRepository.existsByEmail(email)

    override fun existsByUsername(username: String): Boolean = jpaRepository.existsByUserName(username)

    override fun findById(id: Long): UserEntity? = jpaRepository.findById(id).orElse(null)

    override fun findAllByStatus(status: UserStatus): List<UserEntity> = jpaRepository.findAllByStatus(status)

    override fun save(userEntity: UserEntity): UserEntity = jpaRepository.save(userEntity)

    override fun delete(userEntity: UserEntity) {
        jpaRepository.delete(userEntity)
    }

    override fun findAll(): List<UserEntity> = jpaRepository.findAll()
}
