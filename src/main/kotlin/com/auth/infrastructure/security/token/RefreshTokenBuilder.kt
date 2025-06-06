package com.auth.infrastructure.security.token

import com.auth.domain.auth.model.TokenClaim
import com.auth.domain.auth.model.TokenType
import io.jsonwebtoken.JwtBuilder
import java.util.UUID
import javax.crypto.SecretKey

/**
 * 리프레시 토큰 생성을 위한 빌더 구현체
 * 긴 만료 시간을 가진 토큰으로, 액세스 토큰 갱신에 사용됩니다.
 */
internal class RefreshTokenBuilder(
    subject: String,
    expirationMs: Long,
    key: SecretKey,
) : AbstractTokenBuilder(subject, expirationMs, key) {
    override fun customizeBuild(builder: JwtBuilder) {
        // 리프레시 토큰 타입 설정
        builder.claim(TokenClaim.TYPE.value, TokenType.REFRESH.value)

        // 고유 ID 추가 (토큰 무효화에 사용 가능)
        builder.id(UUID.randomUUID().toString())
    }
}
