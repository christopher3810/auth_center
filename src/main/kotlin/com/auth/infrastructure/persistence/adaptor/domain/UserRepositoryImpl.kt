package com.auth.infrastructure.persistence.adaptor

import com.auth.domain.user.model.User
import com.auth.domain.user.repository.UserRepository
import com.auth.infrastructure.repository.UserJpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * UserRepository 도메인 인터페이스의 구현체
 * 
 * 인프라스트럭처 계층에서 JPA 기반 영속성을 제공합니다.
 */
@Repository
class UserRepositoryImpl(
    private val jpaRepository: UserJpaRepository
) : UserRepository {
    
    override fun findByEmail(email: String): Optional<User> {
        return jpaRepository.findByEmail(email)
    }
    
    override fun existsByEmail(email: String): Boolean {
        return jpaRepository.existsByEmail(email)
    }
    
    override fun findById(id: Long): Optional<User> {
        return jpaRepository.findById(id)
    }
    
    override fun save(user: User): User {
        return jpaRepository.save(user)
    }
    
    override fun delete(user: User) {
        jpaRepository.delete(user)
    }
    
    override fun findAll(): List<User> {
        return jpaRepository.findAll()
    }
} 