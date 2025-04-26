package com.auth.infrastructure.cache.adaptor

import com.auth.application.cache.StringCacheAdaptor
import com.auth.application.cache.exception.CacheException
import com.auth.infrastructure.cache.AbstractRedisCacheManager
import com.auth.infrastructure.cache.config.CacheProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * StringCacheManager 인터페이스의 Redis 구현
 * 문자열 관련 Redis 작업을 처리합니다.
 */
@Component
class StringRedisCacheAdaptorImpl(
    redisTemplate: RedisTemplate<String, Any>,
    cacheProperties: CacheProperties,
) : AbstractRedisCacheManager(redisTemplate, cacheProperties),
    StringCacheAdaptor {
    override fun setString(
        key: String,
        value: String,
    ) {
        executeRedisOperation(
            operation = {
                redisTemplate.opsForValue().set(key, value, getEffectiveTtl(null))
                logger.debug { "문자열 저장 성공: $key" }
                true
            },
            errorMessage = "문자열 저장 실패: $key",
        )
    }

    override fun getString(key: String): String? =
        executeRedisOperation(
            operation = {
                val value = redisTemplate.opsForValue().get(key)

                when (value) {
                    null -> {
                        logger.debug { "문자열 조회 결과 없음: $key" }
                        null
                    }
                    is String -> {
                        logger.debug { "문자열 조회 성공: $key" }
                        value
                    }
                    else -> {
                        logger.warn { "문자열로 변환할 수 없는 값: $key, 타입: ${value.javaClass.name}" }
                        value.toString()
                    }
                }
            },
            errorMessage = "문자열 조회 실패: $key",
        )

    override fun increment(
        key: String,
        delta: Long,
    ): Long {
        return executeRedisOperation(
            operation = {
                val result = redisTemplate.opsForValue().increment(key, delta)
                if (result == null) {
                    logger.warn { "증가 작업 실패, null 반환됨: $key" }
                    throw CacheException.OperationException("증가 작업 실패: $key")
                }

                // 첫 증가 작업인 경우 만료 시간 설정
                if (result == delta) {
                    setExpire(key, getEffectiveTtl(null))
                }

                logger.debug { "증가 작업 성공: $key, 증가량: $delta, 결과: $result" }
                result
            },
            errorMessage = "증가 작업 실패: $key, 증가량: $delta",
        ) ?: delta // 실패 시 기본값 반환
    }
}
