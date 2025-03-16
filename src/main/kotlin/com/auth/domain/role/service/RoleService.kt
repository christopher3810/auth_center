package com.auth.domain.role.service

import com.auth.domain.role.entity.RoleEntity
import com.auth.domain.role.factory.RoleFactory
import com.auth.domain.role.model.RoleModel
import com.auth.domain.role.repository.RoleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

/**
 * 역할 도메인 서비스
 */
@Service
class RoleService(
    private val roleRepository: RoleRepository
) {

    /**
     * ID로 역할 조회
     */
    @Transactional(readOnly = true)
    fun findRoleById(id: Long): Optional<RoleModel> {
        // 1. Entity 조회
        val roleEntityOpt = roleRepository.findById(id)
        
        // 2. Entity -> Model 변환 (Factory 사용)
        return roleEntityOpt.map { RoleFactory.createFromEntity(it) }
    }
    
    /**
     * 역할 코드로 역할 조회
     */
    @Transactional(readOnly = true)
    fun findRoleByCode(code: String): Optional<RoleModel> {
        // 1. Entity 조회
        val roleEntityOpt = roleRepository.findByCode(code)
        
        // 2. Entity -> Model 변환 (Factory 사용)
        return roleEntityOpt.map { RoleFactory.createFromEntity(it) }
    }
    
    /**
     * 모든 역할 조회
     */
    @Transactional(readOnly = true)
    fun findAllRoles(): List<RoleModel> {
        // 1. Entity 목록 조회
        val roleEntities = roleRepository.findAll()
        
        // 2. Entity -> Model 변환 (Factory 사용)
        return roleEntities.map { RoleFactory.createFromEntity(it) }
    }
    
    /**
     * 관리자 역할 생성 또는 조회
     * 시스템에 관리자 역할이 없는 경우 생성하고, 있는 경우 기존 역할 반환
     */
    @Transactional
    fun getOrCreateAdminRole(): RoleModel {
        val adminCode = "ROLE_ADMIN"
        
        // 관리자 역할이 이미 존재하는지 확인
        val existingRole = roleRepository.findByCode(adminCode)
        
        if (existingRole.isPresent) {
            return RoleFactory.createFromEntity(existingRole.get())
        }
        
        // 관리자 역할이 없으면 생성
        val adminRole = RoleFactory.createAdminRole()
        val entity = RoleFactory.createEntity(adminRole)
        val savedEntity = roleRepository.save(entity)
        
        return RoleFactory.createFromEntity(savedEntity)
    }
    
    /**
     * 일반 사용자 역할 생성 또는 조회
     * 시스템에 일반 사용자 역할이 없는 경우 생성하고, 있는 경우 기존 역할 반환
     */
    @Transactional
    fun getOrCreateUserRole(): RoleModel {
        val userCode = "ROLE_USER"
        
        // 일반 사용자 역할이 이미 존재하는지 확인
        val existingRole = roleRepository.findByCode(userCode)
        
        if (existingRole.isPresent) {
            return RoleFactory.createFromEntity(existingRole.get())
        }
        
        // 일반 사용자 역할이 없으면 생성
        val userRole = RoleFactory.createUserRole()
        val entity = RoleFactory.createEntity(userRole)
        val savedEntity = roleRepository.save(entity)
        
        return RoleFactory.createFromEntity(savedEntity)
    }
    
    /**
     * 새 역할 생성
     */
    @Transactional
    fun createRole(
        code: String,
        name: String,
        description: String? = null,
        permissions: Set<String> = setOf()
    ): RoleModel {
        // 역할 코드 중복 검사
        val normalizedCode = if (code.startsWith("ROLE_")) code else "ROLE_$code"
        
        if (roleRepository.existsByCode(normalizedCode)) {
            throw IllegalArgumentException("이미 존재하는 역할 코드입니다: $normalizedCode")
        }
        
        // Factory를 통해 도메인 모델 생성
        val roleModel = RoleFactory.createRole(
            code = code,
            name = name,
            description = description,
            permissions = permissions
        )
        
        // 도메인 모델 -> Entity 변환 및 저장
        val entity = RoleFactory.createEntity(roleModel)
        val savedEntity = roleRepository.save(entity)
        
        return RoleFactory.createFromEntity(savedEntity)
    }
    
    /**
     * 역할 모델 저장
     */
    @Transactional
    fun saveRole(roleModel: RoleModel): RoleModel {
        // 1. Entity 생성 또는 업데이트
        val roleEntity: RoleEntity = if (roleModel.id == 0L) {
            // 신규 역할인 경우
            // 역할 코드 중복 검사
            if (roleRepository.existsByCode(roleModel.code)) {
                throw IllegalArgumentException("이미 존재하는 역할 코드입니다: ${roleModel.code}")
            }
            
            RoleFactory.createEntity(roleModel)
        } else {
            // 기존 역할 업데이트인 경우
            val existingEntity = roleRepository.findById(roleModel.id)
                .orElseThrow { IllegalArgumentException("존재하지 않는 역할 ID: ${roleModel.id}") }
            
            // Entity 업데이트
            RoleFactory.updateEntity(existingEntity, roleModel)
        }
        
        // 2. Entity 저장
        val savedEntity = roleRepository.save(roleEntity)
        
        // 3. 저장된 Entity -> 도메인 모델 변환 (Factory 사용)
        return RoleFactory.createFromEntity(savedEntity)
    }
    
    /**
     * 역할 삭제
     */
    @Transactional
    fun deleteRole(roleModel: RoleModel) {
        // 시스템 기본 역할은 삭제 방지
        if (roleModel.code == "ROLE_ADMIN" || roleModel.code == "ROLE_USER") {
            throw IllegalArgumentException("시스템 기본 역할은 삭제할 수 없습니다: ${roleModel.code}")
        }
        
        // 1. ID로 엔티티 조회
        roleRepository.findById(roleModel.id).ifPresent { entity ->
            // 2. 엔티티 삭제
            roleRepository.delete(entity)
        }
    }
} 