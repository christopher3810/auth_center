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
    key: SecretKey
) : AbstractTokenBuilder(subject, expirationMs, key) {
    
    /**
     * 사용자 역할 정보를 토큰에 추가합니다.
     *
     * @param roles 사용자 역할 목록
     * @return TokenBuilder 인스턴스
     */
    fun withRoles(roles: List<String>): TokenBuilder {
        return withClaim(TokenClaim.ROLES.value, roles.joinToString(","))
    }

    /**
     * 사용자 권한 정보를 토큰에 추가합니다.
     *
     * @param permissions 사용자 권한 목록
     * @return TokenBuilder 인스턴스
     */
    fun withPermissions(permissions: List<String>): TokenBuilder {
        return withClaim(TokenClaim.PERMISSIONS.value, permissions.joinToString(","))
    }

    override fun customizeBuild(builder: JwtBuilder) {
        // 권한 검증용 토큰 타입 설정
        builder.claim(TokenClaim.TYPE.value, TokenType.AUTHORIZATION.value)
        
        // 기본 역할 클레임이 없다면 빈 값 설정
        if (!claims.containsKey(TokenClaim.ROLES.value)) {
            builder.claim(TokenClaim.ROLES.value, "")
        }
        
        // 기본 권한 클레임이 없다면 빈 값 설정
        if (!claims.containsKey(TokenClaim.PERMISSIONS.value)) {
            builder.claim(TokenClaim.PERMISSIONS.value, "")
        }
    }
} 