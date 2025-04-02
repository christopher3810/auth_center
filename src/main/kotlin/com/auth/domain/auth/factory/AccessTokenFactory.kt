package com.auth.domain.auth.factory

import com.auth.domain.auth.model.AccessToken
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * 액세스 토큰 도메인 팩토리
 */
@Component
class AccessTokenFactory {
    /**
     * 새 액세스 토큰 생성
     *
     * @param tokenValue 실제 토큰 문자열 (예: JWT)
     * @param userId 사용자 식별자
     * @param subject 사용자 이메일 또는 식별자
     * @param validityInSeconds 토큰 유효 기간(초)
     * @param roles 사용자 역할 목록 (기본값: emptySet)
     * @param permissions 사용자 권한 목록 (기본값: emptySet)
     * @return 생성된 AccessToken 도메인 모델
     */
    fun createAccessToken(
        tokenValue: String,
        userId: Long,
        subject: String,
        validityInSeconds: Long,
        roles: Set<String> = emptySet(),
        permissions: Set<String> = emptySet(),
    ): AccessToken {
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plus(validityInSeconds, ChronoUnit.SECONDS)
        return AccessToken(
            tokenValue = tokenValue,
            userId = userId,
            subject = subject,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            roles = roles,
            permissions = permissions,
        )
    }
}
