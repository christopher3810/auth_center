package com.auth.infrastructure.repository.domain

import com.auth.domain.role.entity.RoleEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RoleJpaRepository : JpaRepository<RoleEntity, Long> {
    fun findByCode(code: String): Optional<RoleEntity>

    fun save(roleEntity: RoleEntity): RoleEntity

    fun existsByCode(code: String): Boolean
}
