package com.auth.domain.user

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @Column(nullable = false)
    var password: String,
    
    @Column(nullable = false)
    var name: String,
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    var roles: MutableSet<String> = mutableSetOf(),
    
    @Column(nullable = false)
    var enabled: Boolean = true,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 사용자에게 역할 추가
     */
    fun addRole(role: String) {
        roles.add(role)
    }
    
    /**
     * 사용자에게서 역할 제거
     */
    fun removeRole(role: String) {
        roles.remove(role)
    }
    
    /**
     * 사용자가 특정 역할을 가지고 있는지 확인
     */
    fun hasRole(role: String): Boolean {
        return roles.contains(role)
    }
    
    /**
     * 사용자 정보 업데이트 시 호출
     */
    fun update(name: String? = null, password: String? = null) {
        name?.let { this.name = it }
        password?.let { this.password = it }
        this.updatedAt = LocalDateTime.now()
    }
} 