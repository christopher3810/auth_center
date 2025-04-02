package com.auth.infrastructure.security.token

import com.auth.domain.auth.model.TokenClaim
import com.auth.domain.auth.model.TokenType
import io.jsonwebtoken.JwtBuilder
import javax.crypto.SecretKey

/**
 * 권한 검증용 토큰 생성을 위한 빌더 구현체
 * 사용자의 역할과 권한 정보를 포함하는 액세스 토큰입니다.
 */
internal class AuthorizationTokenBuilder(
    subject: String,
    expirationMs: Long,
    key: SecretKey,
    private val roles: Set<String>,
    private val permissions: Set<String>,
) : AbstractTokenBuilder(subject, expirationMs, key) {
    override fun customizeBuild(builder: JwtBuilder) {
        builder.apply {
            claim(TokenClaim.TYPE.value, TokenType.AUTHORIZATION.value)

            when {
                roles.isNotEmpty() -> claim(TokenClaim.ROLES.value, roles.joinToString(","))
                else -> claim(TokenClaim.ROLES.value, "")
            }

            when {
                permissions.isNotEmpty() -> claim(TokenClaim.PERMISSIONS.value, permissions.joinToString(","))
                else -> claim(TokenClaim.PERMISSIONS.value, "")
            }
        }
    }
}
