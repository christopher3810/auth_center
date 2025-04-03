package com.auth.domain.user.entity

import com.auth.domain.user.value.Email
import com.auth.domain.user.value.Password
import com.auth.domain.user.value.UserStatus
import com.auth.infrastructure.audit.Traceable
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 사용자 엔티티 클래스
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    /**
     * 사용자 로그인 계정명
     */
    @Column(nullable = false, unique = true)
    val userName: String,
    /**
     * 사용자 이메일
     */
    @Embedded
    val email: Email,
    /**
     * 사용자 비밀번호 (해시값)
     */
    @Embedded
    var password: Password,
    /**
     * 사용자 이름
     */
    @Column(nullable = false)
    var name: String,
    /**
     * 사용자 전화번호
     */
    @Column(nullable = true)
    var phoneNumber: String? = null,
    /**
     * 사용자 역할
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    var roles: MutableSet<String> = mutableSetOf(),
    /**
     * 사용자 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus = UserStatus.INACTIVE,
    /**
     * 마지막 로그인 일시
     */
    @Column(nullable = true)
    var lastLoginAt: LocalDateTime? = null,
    @Embedded
    val traceable: Traceable = Traceable(),
)
