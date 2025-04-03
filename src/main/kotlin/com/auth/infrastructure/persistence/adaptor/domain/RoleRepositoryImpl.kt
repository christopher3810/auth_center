package com.auth.infrastructure.persistence.adaptor.domain

import com.auth.domain.role.entity.RoleEntity
import com.auth.domain.role.repository.RoleRepository
import com.auth.infrastructure.repository.domain.RoleJpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
class RoleRepositoryImpl(
    private val roleJpaRepository: RoleJpaRepository,
) : RoleRepository {
    override fun findById(id: Long): Optional<RoleEntity> = roleJpaRepository.findById(id)

    override fun findByCode(code: String): Optional<RoleEntity> = roleJpaRepository.findByCode(code)

    override fun findAll(): List<RoleEntity> = roleJpaRepository.findAll()

    override fun save(roleEntity: RoleEntity): RoleEntity = roleJpaRepository.save(roleEntity)

    override fun delete(roleEntity: RoleEntity) = roleJpaRepository.delete(roleEntity)

    override fun existsByCode(code: String): Boolean = roleJpaRepository.existsByCode(code)
}
