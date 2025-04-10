package com.auth.infrastructure.security.token

import com.auth.domain.auth.model.TokenClaim
import com.auth.domain.auth.model.TokenType
import io.jsonwebtoken.JwtBuilder
import java.util.UUID
import javax.crypto.SecretKey

/**
 * 일회용 토큰 생성을 위한 빌더 구현체
 * 이메일 인증, 비밀번호 재설정 등 일회성 작업에 사용되는 토큰입니다.
 */
internal class OneTimeTokenBuilder(
    subject: String,
    expirationMs: Long,
    key: SecretKey,
    private val purpose: String,
) : AbstractTokenBuilder(subject, expirationMs, key) {
    init {
        // 기본적으로 사용되지 않은 상태로 초기화
        withClaim(TokenClaim.USED.value, false)
    }

    override fun customizeBuild(builder: JwtBuilder) {
        // 일회용 토큰 타입 설정
        builder.claim(TokenClaim.TYPE.value, TokenType.ONE_TIME.value)

        // 토큰 목적 설정 (EMAIL_VERIFICATION, PASSWORD_RESET 등)
        builder.claim(TokenClaim.PURPOSE.value, purpose)

        // 고유 ID 추가 (토큰 추적에 사용)
        builder.id(UUID.randomUUID().toString())
    }
}
