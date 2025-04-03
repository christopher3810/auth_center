package com.auth.domain.role.entity

import com.auth.infrastructure.audit.Traceable
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table

/**
 * 역할 엔티티 클래스
 */
@Entity
@Table(name = "roles")
class RoleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    /**
     * 역할 코드 (고유 식별자로 사용)
     */
    @Column(nullable = false, unique = true, length = 50)
    val code: String,
    /**
     * 역할 이름
     */
    @Column(nullable = false, length = 100)
    var name: String,
    /**
     * 역할 설명
     */
    @Column(nullable = true, length = 255)
    var description: String? = null,
    /**
     * 역할에 연결된 권한 목록
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = [JoinColumn(name = "role_id")])
    @Column(name = "permission")
    var permissions: MutableSet<String> = mutableSetOf(),
    @Embedded
    val traceable: Traceable = Traceable(),
)
