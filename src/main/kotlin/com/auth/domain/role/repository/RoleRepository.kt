package com.auth.domain.role.repository

import com.auth.domain.role.entity.RoleEntity
import java.util.Optional

/**
 * 역할 도메인 리포지토리 인터페이스
 */
interface RoleRepository {
    /**
     * ID로 역할 엔티티 찾기
     */
    fun findById(id: Long): Optional<RoleEntity>

    /**
     * 역할 코드로 역할 엔티티 찾기
     */
    fun findByCode(code: String): Optional<RoleEntity>

    /**
     * 모든 역할 엔티티 조회
     */
    fun findAll(): List<RoleEntity>

    /**
     * 역할 엔티티 저장
     */
    fun save(roleEntity: RoleEntity): RoleEntity

    /**
     * 역할 엔티티 삭제
     */
    fun delete(roleEntity: RoleEntity)

    /**
     * 역할 코드 존재 여부 확인
     */
    fun existsByCode(code: String): Boolean
}
