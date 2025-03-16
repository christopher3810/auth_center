package com.auth.domain.auth.factory

import com.auth.domain.auth.entity.RefreshTokenEntity
import com.auth.domain.auth.event.TokenCreatedEvent
import com.auth.domain.auth.model.RefreshToken
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 리프레시 토큰 도메인 팩토리
 *
 * Eric Evans DDD 원칙에 따른 Factory 패턴 구현:
 * "생성 과정이 복잡하거나 생성자가 의도를 명확히 표현하지 못하는 경우, 
 * 또는 도메인 개체 생성 시 규칙을 지켜야 할 때 팩토리가 필요합니다."
 */
@Component
class RefreshTokenFactory {
    
    companion object {
        /**
         * 새 리프레시 토큰 생성
         */
        fun createToken(
            token: String,
            userId: Long,
            userEmail: String,
            expiryTimeInMinutes: Long
        ): RefreshToken {
            val expiryDate = LocalDateTime.now().plusMinutes(expiryTimeInMinutes)
            
            val refreshToken = RefreshToken(
                token = token,
                userId = userId,
                userEmail = userEmail,
                expiryDate = expiryDate,
                used = false,
                revoked = false,
                createdAt = LocalDateTime.now()
            )
            
            // 토큰 생성 이벤트 발행
            refreshToken.registerEvent(TokenCreatedEvent(refreshToken))
            
            return refreshToken
        }
        
        /**
         * 리프레시 토큰 엔티티로부터 도메인 모델 생성
         */
        fun createFromEntity(entity: RefreshTokenEntity): RefreshToken {
            return RefreshToken(
                id = entity.id,
                token = entity.token,
                userId = entity.userId,
                userEmail = entity.userEmail,
                expiryDate = entity.expiryDate,
                used = entity.used,
                revoked = entity.revoked,
                createdAt = entity.traceable.createdAt,
                updatedAt = entity.traceable.updatedAt
            )
        }
        
        /**
         * 도메인 모델로부터 리프레시 토큰 엔티티 생성
         */
        fun createEntity(model: RefreshToken): RefreshTokenEntity {
            return RefreshTokenEntity(
                id = model.id,
                token = model.token,
                userId = model.userId,
                userEmail = model.userEmail,
                expiryDate = model.expiryDate,
                used = model.used,
                revoked = model.revoked
            )
        }
        
        /**
         * 모델을 기반으로 기존 엔티티 업데이트
         */
        fun updateEntity(entity: RefreshTokenEntity, model: RefreshToken): RefreshTokenEntity {
            entity.apply {
                // 변경 가능한 필드만 업데이트
                this.used = model.used
                this.revoked = model.revoked
            }
            
            return entity
        }
    }
} 