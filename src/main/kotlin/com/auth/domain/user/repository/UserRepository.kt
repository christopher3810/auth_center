package com.auth.domain.user.repository

import com.auth.domain.user.model.User
import java.util.Optional

/**
 * 사용자 도메인 리포지토리 인터페이스
 * 
 * DDD 원칙에 따라 도메인 계층에 위치하며, 도메인 객체의 영속성 추상화를 담당합니다.
 * 구현체는 인프라스트럭처 계층에 존재합니다.
 */
interface UserRepository {
    
    /**
     * 이메일로 사용자 찾기
     */
    fun findByEmail(email: String): Optional<User>
    
    /**
     * 이메일 존재 여부 확인
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * 사용자 ID로 사용자 찾기
     */
    fun findById(id: Long): Optional<User>
    
    /**
     * 사용자 저장
     */
    fun save(user: User): User
    
    /**
     * 사용자 삭제
     */
    fun delete(user: User)
    
    /**
     * 모든 사용자 찾기
     */
    fun findAll(): List<User>
} 