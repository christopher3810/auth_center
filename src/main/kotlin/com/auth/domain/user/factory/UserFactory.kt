package com.auth.domain.user.factory

import com.auth.domain.user.entity.UserEntity
import com.auth.domain.user.model.User
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.Password
import com.auth.domain.user.value.UserStatus
import java.time.LocalDateTime

/**
 * 사용자 도메인 팩토리
 */
class UserFactory {
    
    companion object {
        /**
         * 사용자 도메인 모델 생성 - 일반 사용자 등록용 (자체 가입)
         * Self-Service 등록 시에는 미인증 상태로 생성되고 이메일 검증을 거쳐 활성화."
         */
        fun createUser(
            username: String,
            email: String,
            rawPassword: String,
            name: String,
            phoneNumber: String? = null,
            initialRoles: Set<String> = setOf()
        ): User {
            // email, password vo 생성 (유효성 검사 포함)
            val emailObj = Email(email)
            val passwordObj = Password.of(rawPassword)

            return User(
                username = username,
                email = emailObj,
                password = passwordObj,
                name = name,
                phoneNumber = phoneNumber,
                roles = initialRoles,
                status = UserStatus.INACTIVE,
                createdAt = LocalDateTime.now()
            )
        }
        
        /**
         * 사용자 도메인 모델 생성 - 관리자에 의한 등록용
         * 관리자가 생성하는 경우 바로 활성화할 수 있음
         */
        fun createUserByAdmin(
            username: String,
            email: String,
            rawPassword: String,
            name: String,
            phoneNumber: String? = null,
            initialRoles: Set<String> = setOf("ROLE_USER")
        ): User {
            // email, password vo 생성 (유효성 검사 포함)
            val emailObj = Email(email)
            val passwordObj = Password.of(rawPassword)
            
            // 사용자 모델 생성 (활성 상태로 시작)
            return User(
                username = username,
                email = emailObj,
                password = passwordObj,
                name = name,
                phoneNumber = phoneNumber,
                roles = initialRoles,
                status = UserStatus.ACTIVE,
                createdAt = LocalDateTime.now()
            )
        }
        
        /**
         * 사용자 엔티티로부터 도메인 모델 생성
         */
        fun createFromEntity(entity: UserEntity): User {
            return User(
                id = entity.id,
                username = entity.userName,
                email = entity.email,
                password = entity.password,
                name = entity.name,
                phoneNumber = entity.phoneNumber,
                roles = entity.roles.toSet(),
                status = entity.status,
                lastLoginAt = entity.lastLoginAt,
                createdAt = entity.traceable.createdAt,
                updatedAt = entity.traceable.updatedAt
            )
        }
        
        /**
         * 도메인 모델로부터 사용자 엔티티 생성
         */
        fun createEntity(model: User): UserEntity {
            return UserEntity(
                id = model.id,
                userName = model.username,
                email = model.email,
                password = model.password,
                name = model.name,
                phoneNumber = model.phoneNumber,
                roles = model.roles.toMutableSet(),
                status = model.status,
                lastLoginAt = model.lastLoginAt
            )
        }
        
        /**
         * 모델을 기반으로 기존 엔티티 업데이트
         */
        fun updateEntity(entity: UserEntity, model: User): UserEntity {

            // 변경 불가능한 필드를 제외한 업데이트 가능한 필드만 적용
            entity.apply { 
                this.name = model.name
                this.phoneNumber = model.phoneNumber
                this.password = model.password
                this.status = model.status
                this.lastLoginAt = model.lastLoginAt
                
                // 역할 업데이트
                this.roles.clear()
                this.roles.addAll(model.roles)
            }
            
            return entity
        }
    }
} 