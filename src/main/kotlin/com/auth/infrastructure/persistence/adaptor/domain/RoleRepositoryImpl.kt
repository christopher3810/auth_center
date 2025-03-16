package com.auth.infrastructure.persistence.adaptor.domain

import com.auth.domain.role.entity.RoleEntity
import com.auth.domain.role.repository.RoleRepository
import com.auth.infrastructure.repository.domain.RoleJpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class RoleRepositoryImpl(
    private val roleJpaRepository: RoleJpaRepository
): RoleRepository {
    override fun findById(id: Long): Optional<RoleEntity> {
        return roleJpaRepository.findById(id)
    }

    override fun findByCode(code: String): Optional<RoleEntity> {
        return roleJpaRepository.findByCode(code)
    }

    override fun findAll(): List<RoleEntity> {
        return roleJpaRepository.findAll()
    }

    override fun save(roleEntity: RoleEntity): RoleEntity {
        return roleJpaRepository.save(roleEntity)
    }

    override fun delete(roleEntity: RoleEntity) {
        return roleJpaRepository.delete(roleEntity)
    }

    override fun existsByCode(code: String): Boolean {
        return roleJpaRepository.existsByCode(code)
    }
}