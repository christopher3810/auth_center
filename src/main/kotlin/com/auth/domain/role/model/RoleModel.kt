package com.auth.domain.role.model

import java.time.LocalDateTime

/**
 * 역할 도메인 모델
 * 필요에 따라 권한(Permission)이나 그룹(Group) 등의 개념을 추가할 수 있음,
 * 기본 모델링에서는 역할에 권한을 포함하는 것으로 단순화
 */
class RoleModel(
    val id: Long = 0,
    val code: String,
    var name: String,
    var description: String? = null,
    var permissions: Set<String> = setOf(),
    val createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null
) {
    /**
     * 권한 추가
     */
    fun addPermission(permission: String): RoleModel {
        permissions = permissions + permission
        return this
    }
    
    /**
     * 권한 제거
     */
    fun removePermission(permission: String): RoleModel {
        if (permission in permissions) {
            permissions = permissions - permission
        }
        return this
    }
    
    /**
     * 특정 권한을 가지고 있는지 확인
     */
    fun hasPermission(permission: String): Boolean {
        return permission in permissions
    }
    
    /**
     * 권한 일괄 변경
     */
    fun updatePermissions(newPermissions: Set<String>): RoleModel {
        permissions = newPermissions
        return this
    }
    
    /**
     * 역할 정보 업데이트 (이름, 설명)
     */
    fun update(name: String? = null, description: String? = null): RoleModel {
        name?.let { this.name = it }
        description?.let { this.description = it }
        updatedAt = LocalDateTime.now()
        return this
    }
} 