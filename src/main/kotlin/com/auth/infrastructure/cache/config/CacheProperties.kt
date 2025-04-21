package com.auth.infrastructure.cache.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * 캐시 관련 설정 프로퍼티
 * 캐시 서비스의 다양한 설정 값을 관리합니다.
 */
@ConfigurationProperties(prefix = "app.cache")
data class CacheProperties(
    /**
     * 캐시 사용 여부
     */
    val enabled: Boolean = true,
    /**
     * 기본 캐시 만료 시간
     */
    val ttl: Duration = Duration.ofMinutes(30),
    /**
     * 토큰 블랙리스트 캐시 만료 시간
     */
    val tokenBlacklistTtl: Duration = Duration.ofDays(7),
    /**
     * 속도 제한 관련 캐시 설정
     */
    val rateLimiting: RateLimitingCacheProperties = RateLimitingCacheProperties(),
)


/**
 * 속도 제한 관련 캐시 설정 (사용여부는 고민중)
 */
data class RateLimitingCacheProperties(
    /**
     * 로그인 시도 캐시 만료 시간
     */
    val loginAttempts: Duration = Duration.ofMinutes(10),
    /**
     * API 요청 속도 제한 캐시 만료 시간
     */
    val apiRequests: Duration = Duration.ofMinutes(1),
) 
