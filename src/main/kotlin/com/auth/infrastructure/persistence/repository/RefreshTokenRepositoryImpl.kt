package com.auth.infrastructure.persistence.repository

import com.auth.domain.auth.model.RefreshToken
import com.auth.domain.auth.repository.RefreshTokenRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.Optional

/**
 * JPA 기반 RefreshTokenRepository 구현체를 위한 인터페이스
 */
@Repository
interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): Optional<RefreshToken>
    
    fun findByUserId(userId: Long): List<RefreshToken>
    
    @Query("SELECT r FROM RefreshToken r WHERE r.userId = :userId AND r.used = false AND r.revoked = false AND r.expiryDate > :currentTime")
    fun findByUserIdAndUsedFalseAndRevokedFalseAndExpiryDateAfter(
        userId: Long, 
        currentTime: LocalDateTime
    ): List<RefreshToken>
    
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.userId = :userId")
    fun revokeAllUserTokens(userId: Long): Int
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    fun deleteAllExpiredTokens(now: LocalDateTime): Int
}

/**
 * RefreshTokenRepository 도메인 인터페이스의 구현체
 * 
 * 인프라스트럭처 계층에서 JPA 기반 영속성을 제공합니다.
 */
@Repository
class RefreshTokenRepositoryImpl(
    private val jpaRepository: RefreshTokenJpaRepository
) : RefreshTokenRepository {
    
    override fun findByToken(token: String): Optional<RefreshToken> {
        return jpaRepository.findByToken(token)
    }
    
    override fun findByUserId(userId: Long): List<RefreshToken> {
        return jpaRepository.findByUserId(userId)
    }
    
    override fun findByUserIdAndValidTrue(userId: Long, currentTime: LocalDateTime): List<RefreshToken> {
        return jpaRepository.findByUserIdAndUsedFalseAndRevokedFalseAndExpiryDateAfter(userId, currentTime)
    }
    
    override fun revokeAllUserTokens(userId: Long): Int {
        return jpaRepository.revokeAllUserTokens(userId)
    }
    
    override fun deleteAllExpiredTokens(now: LocalDateTime): Int {
        return jpaRepository.deleteAllExpiredTokens(now)
    }
    
    override fun save(refreshToken: RefreshToken): RefreshToken {
        return jpaRepository.save(refreshToken)
    }
    
    override fun delete(refreshToken: RefreshToken) {
        jpaRepository.delete(refreshToken)
    }
    
    override fun findById(id: Long): Optional<RefreshToken> {
        return jpaRepository.findById(id)
    }
} 