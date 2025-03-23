package com.auth.domain.user.repository

import com.auth.domain.user.entity.UserEntity
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.UserStatus

/**
 * 사용자 도메인 리포지토리 인터페이스 도메인 객체의 영속성 추상화.
 */
interface UserRepository {
    
    /**
     * 이메일로 사용자 엔티티 찾기
     * email: 이메일 주소. 필요 시 Email 타입의 값 객체로 정의하여 유효성(@) 포함 등)을 불변식으로 보장합니다."
     */
    fun findByEmail(email: Email): UserEntity?
    
    /**
     * 사용자명으로 사용자 엔티티 찾기
     */
    fun findByUsername(username: String): UserEntity?
    
    /**
     * 이메일 존재 여부 확인
     */
    fun existsByEmail(email: Email): Boolean
    
    /**
     * 사용자명 존재 여부 확인
     */
    fun existsByUsername(username: String): Boolean
    
    /**
     * 사용자 ID로 사용자 엔티티 찾기
     */
    fun findById(id: Long): UserEntity?
    
    /**
     * 상태로 사용자 엔티티 목록 찾기
     */
    fun findAllByStatus(status: UserStatus): List<UserEntity>
    
    /**
     * 사용자 엔티티 저장
     */
    fun save(userEntity: UserEntity): UserEntity
    
    /**
     * 사용자 엔티티 삭제
     */
    fun delete(userEntity: UserEntity)
    
    /**
     * 모든 사용자 엔티티 찾기
     */
    fun findAll(): List<UserEntity>
} 