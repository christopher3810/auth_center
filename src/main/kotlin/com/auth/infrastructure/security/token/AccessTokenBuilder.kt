package com.auth.infrastructure.security.token

import com.auth.domain.auth.model.TokenClaim
import com.auth.domain.auth.model.TokenType
import io.jsonwebtoken.JwtBuilder
import javax.crypto.SecretKey

/**
 * 액세스 토큰 생성을 위한 빌더 구현체
 * 짧은 만료 시간을 가진 일반적인 인증용 토큰을 생성합니다.
 */
@Deprecated("Access light 한 Token 으로 별도로 분리를 했었으나 필요성을 느끼지 못하고 있음")
internal class AccessTokenBuilder(
    subject: String,
    expirationMs: Long,
    key: SecretKey,
) : AbstractTokenBuilder(subject, expirationMs, key) {
    override fun customizeBuild(builder: JwtBuilder) {
        // 액세스 토큰 타입 설정
        builder.claim(TokenClaim.TYPE.value, TokenType.ACCESS.value)
    }
}
