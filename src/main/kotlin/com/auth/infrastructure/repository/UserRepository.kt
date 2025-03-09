package com.auth.infrastructure.repository

import com.auth.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * 사용자 정보에 접근하기 위한 리포지토리
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 찾기
     */
    fun findByEmail(email: String): Optional<User>
    
    /**
     * 이메일 존재 여부 확인
     */
    fun existsByEmail(email: String): Boolean
} 