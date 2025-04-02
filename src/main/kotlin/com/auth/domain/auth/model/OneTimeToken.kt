package com.auth.domain.auth.model

import java.time.Instant

/**
 * 일회용 토큰 도메인 모델
 * 이메일 인증, 비밀번호 재설정 등의 목적으로 사용되는 단기 토큰.
 */
class OneTimeToken(
    val tokenValue: String,
    val userId: Long,
    val subject: String,
    val purpose: TokenPurpose,
    val issuedAt: Instant,
    val expiresAt: Instant,
    var used: Boolean = false,
) {
    /**
     * 토큰이 만료되었는지 확인
     */
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    /**
     * 토큰이 유효한지 확인 (만료되지 않고, 사용되지 않음)
     */
    fun isValid(): Boolean = !isExpired() && !used

    /**
     * 토큰 사용 처리
     */
    fun markAsUsed(): OneTimeToken {
        used = true
        return this
    }

    /**
     * 특정 목적의 토큰인지 확인
     */
    fun hasCorrectPurpose(purpose: TokenPurpose): Boolean = this.purpose == purpose

    companion object {
        /**
         * 이메일 인증 토큰 생성
         */
        fun createEmailVerificationToken(
            tokenValue: String,
            userId: Long,
            email: String,
            expiryTimeInMinutes: Long = 30,
        ): OneTimeToken {
            val now = Instant.now()
            val expiresAt = now.plusSeconds(expiryTimeInMinutes * 60)

            return OneTimeToken(
                tokenValue = tokenValue,
                userId = userId,
                subject = email,
                purpose = TokenPurpose.EMAIL_VERIFICATION,
                issuedAt = now,
                expiresAt = expiresAt,
            )
        }

        /**
         * 비밀번호 재설정 토큰 생성
         */
        fun createPasswordResetToken(
            tokenValue: String,
            userId: Long,
            email: String,
            expiryTimeInMinutes: Long = 15,
        ): OneTimeToken {
            val now = Instant.now()
            val expiresAt = now.plusSeconds(expiryTimeInMinutes * 60)

            return OneTimeToken(
                tokenValue = tokenValue,
                userId = userId,
                subject = email,
                purpose = TokenPurpose.PASSWORD_RESET,
                issuedAt = now,
                expiresAt = expiresAt,
            )
        }

        /**
         * 계정 활성화 토큰 생성
         */
        fun createAccountActivationToken(
            tokenValue: String,
            userId: Long,
            email: String,
            expiryTimeInMinutes: Long = 1440, // 24시간
        ): OneTimeToken {
            val now = Instant.now()
            val expiresAt = now.plusSeconds(expiryTimeInMinutes * 60)

            return OneTimeToken(
                tokenValue = tokenValue,
                userId = userId,
                subject = email,
                purpose = TokenPurpose.ACCOUNT_ACTIVATION,
                issuedAt = now,
                expiresAt = expiresAt,
            )
        }
    }
}
