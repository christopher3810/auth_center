package com.auth.infrastructure.persistence.adaptor

import com.auth.domain.auth.entity.RefreshTokenEntity
import com.auth.domain.auth.repository.RefreshTokenRepository
import com.auth.infrastructure.repository.RefreshTokenJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class RefreshTokenRepositoryImpl(
    private val jpaRepository: RefreshTokenJpaRepository
) : RefreshTokenRepository {

    override fun findById(id: Long): RefreshTokenEntity? {
        return jpaRepository.findByIdOrNull(id)
    }

    override fun findByToken(token: String): RefreshTokenEntity? {
        return jpaRepository.findByToken(token)
    }

    override fun findByUserId(userId: Long): List<RefreshTokenEntity> {
        return jpaRepository.findByUserId(userId)
    }

    override fun findByUserIdAndValidTrue(userId: Long, currentTime: LocalDateTime): List<RefreshTokenEntity> {
        return jpaRepository.findValidTokensByUserId(userId, currentTime)
    }

    override fun revokeAllUserTokens(userId: Long): Int {
        return jpaRepository.revokeAllUserTokens(userId)
    }

    override fun deleteAllExpiredTokens(now: LocalDateTime): Int {
        return jpaRepository.deleteAllExpiredTokens(now)
    }

    override fun save(refreshTokenEntity: RefreshTokenEntity): RefreshTokenEntity {
        return jpaRepository.save(refreshTokenEntity)
    }

    override fun delete(refreshTokenEntity: RefreshTokenEntity) {
        jpaRepository.delete(refreshTokenEntity)
    }

}