package com.auth.infrastructure.security.token

import com.auth.domain.auth.service.TokenBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.JwtBuilder
import java.security.Key
import javax.crypto.SecretKey
import java.util.Date

/**
 * TokenBuilder 인터페이스의 기본 구현을 제공하는 추상 클래스
 * 도메인 서비스의 추상화를 구체화한 인프라스트럭처 구현체입니다.
 */
abstract class AbstractTokenBuilder(
    protected val subject: String,
    protected val expirationMs: Long,
    protected val key: SecretKey
) : TokenBuilder {
    protected val claims: MutableMap<String, Any> = mutableMapOf()
    protected var issuedAt: Date = Date()
    
    override fun withClaim(key: String, value: Any): TokenBuilder {
        claims[key] = value
        return this
    }
    
    override fun withClaims(claims: Map<String, Any>): TokenBuilder {
        this.claims.putAll(claims)
        return this
    }
    
    override fun withIssuedAt(issuedAt: Date): TokenBuilder {
        this.issuedAt = issuedAt
        return this
    }
    
    override fun build(): String {
        val now = issuedAt
        val expiryDate = Date(now.time + expirationMs)
        
        val builder = Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(expiryDate)
        
        // 추가 클레임 설정
        addClaims(builder)
        
        // 구현체별 추가 설정
        customizeBuild(builder)
        
        return builder.signWith(key).compact()
    }
    
    /**
     * 기본 클레임을 JWT 빌더에 추가합니다.
     * 
     * @param builder JWT 빌더
     */
    protected open fun addClaims(builder: JwtBuilder) {
        claims.forEach { (key, value) ->
            builder.claim(key, value)
        }
    }
    
    /**
     * 구현체별 추가 설정을 적용합니다.
     * 하위 클래스에서 오버라이드하여 토큰 생성 전략별 커스터마이징을 구현합니다.
     * 
     * @param builder JWT 빌더
     */
    protected open fun customizeBuild(builder: JwtBuilder) {
        // 기본 구현은 아무 작업도 수행하지 않음
    }
} 