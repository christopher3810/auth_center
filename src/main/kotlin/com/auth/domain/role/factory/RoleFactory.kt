package com.auth.domain.role.factory

import com.auth.domain.role.entity.RoleEntity
import com.auth.domain.role.model.RoleModel
import java.time.LocalDateTime

/**
 * 역할 도메인 팩토리
 */
class RoleFactory {
    companion object {
        /**
         * 기본 역할 생성 - 관리자 역할
         */
        fun createAdminRole(): RoleModel =
            RoleModel(
                code = "ROLE_ADMIN",
                name = "관리자",
                description = "시스템 관리자 역할",
                permissions =
                    setOf(
                        "user:read",
                        "user:write",
                        "user:delete",
                        "role:read",
                        "role:write",
                        "role:delete",
                    ),
                createdAt = LocalDateTime.now(),
            )

        /**
         * 기본 역할 생성 - 일반 사용자 역할
         */
        fun createUserRole(): RoleModel =
            RoleModel(
                code = "ROLE_USER",
                name = "일반 사용자",
                description = "일반 사용자 역할",
                permissions = setOf("user:read:self"),
                createdAt = LocalDateTime.now(),
            )

        /**
         * 커스텀 역할 생성
         */
        fun createRole(
            code: String,
            name: String,
            description: String? = null,
            permissions: Set<String> = setOf(),
        ): RoleModel {
            // 역할 코드 규칙 검증 (필수: 'ROLE_' 접두사)
            val finalCode = if (code.startsWith("ROLE_")) code else "ROLE_$code"

            return RoleModel(
                code = finalCode,
                name = name,
                description = description,
                permissions = permissions,
                createdAt = LocalDateTime.now(),
            )
        }

        /**
         * 역할 엔티티로부터 도메인 모델 생성
         */
        fun createFromEntity(entity: RoleEntity): RoleModel =
            RoleModel(
                id = entity.id,
                code = entity.code,
                name = entity.name,
                description = entity.description,
                permissions = entity.permissions.toSet(),
                createdAt = entity.traceable.createdAt,
                updatedAt = entity.traceable.updatedAt,
            )

        /**
         * 도메인 모델로부터 역할 엔티티 생성
         */
        fun createEntity(model: RoleModel): RoleEntity =
            RoleEntity(
                id = model.id,
                code = model.code,
                name = model.name,
                description = model.description,
                permissions = model.permissions.toMutableSet(),
            )

        /**
         * 모델을 기반으로 기존 엔티티 업데이트
         */
        fun updateEntity(
            entity: RoleEntity,
            model: RoleModel,
        ): RoleEntity {
            entity.apply {
                // 변경 가능한 필드만 업데이트
                this.name = model.name
                this.description = model.description

                // 권한 목록 업데이트
                this.permissions.clear()
                this.permissions.addAll(model.permissions)
            }

            return entity
        }
    }
}
