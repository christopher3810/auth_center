package com.auth.domain.user.model

import com.auth.infrastructure.audit.Traceable
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 사용자 도메인 엔티티
 * 도메인 계층의 핵심 개체로, 사용자 관련 비즈니스 로직을 캡슐화합니다.
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
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
    
    @Embedded
    val traceable: Traceable = Traceable()
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
    }
} 