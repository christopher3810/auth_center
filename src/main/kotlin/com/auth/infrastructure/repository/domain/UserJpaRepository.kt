package com.auth.infrastructure.repository

import com.auth.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * 사용자 정보에 접근하기 위한 JPA 리포지토리
 * Spring Data JPA 기능을 활용한 데이터 접근 인터페이스
 */
@Repository
interface UserJpaRepository : JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 찾기
     */
    fun findByEmail(email: String): Optional<User>
    
    /**
     * 이메일 존재 여부 확인
     */
    fun existsByEmail(email: String): Boolean
} 