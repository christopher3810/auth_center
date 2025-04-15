package com.auth.infrastructure.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisConfig(
    private val jwtConfig: JwtConfig,
    private val redisProperties: RedisProperties,
) {
    /**
     * Redis 연결 팩토리 구성
     * Lettuce 클라이언트를 사용하여 Redis 서버에 연결.
     */
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisStandaloneConfiguration =
            RedisStandaloneConfiguration().apply {
                hostName = redisProperties.host
                port = redisProperties.port
                if (redisProperties.password.isNotEmpty()) {
                    setPassword(redisProperties.password)
                }
                database = redisProperties.database
            }

        val clientConfigBuilder = LettuceClientConfiguration.builder()

        if (redisProperties.timeout > 0) {
            clientConfigBuilder.commandTimeout(Duration.ofMillis(redisProperties.timeout))
        }

        return LettuceConnectionFactory(redisStandaloneConfiguration, clientConfigBuilder.build()).apply {
            afterPropertiesSet()
        }
    }

    /**
     * 토큰 저장을 위한 특화된 RedisTemplate 구성
     * 토큰 캐싱에 최적화된 설정을 사용.
     */
    @Bean
    fun tokenRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = StringRedisSerializer()
            // JWT 토큰은 문자열이므로 StringRedisSerializer 사용
            valueSerializer = StringRedisSerializer()
            // Hash 작업을 위한 직렬화 설정
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
            // 트랜잭션 지원 활성화
            setEnableTransactionSupport(true)
            afterPropertiesSet()
        }

    /**
     * 문자열 전용 RedisTemplate 구성
     * 성능 최적화를 위한 문자열 전용 템플릿.
     */
    @Bean
    fun stringRedisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
            afterPropertiesSet()
        }

    /**
     * Redis 캐시 매니저 설정
     * JWT 토큰 설정에서 지정한 유효기간에 맞춰 캐시 TTL을 설정.
     */
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val defaultCacheConfig =
            RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()),
                ).serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()),
                ).disableCachingNullValues()

        // JWT 설정에서 토큰 유효기간 가져오기
        val configMap =
            mapOf(
                "accessTokens" to defaultCacheConfig.entryTtl(Duration.ofSeconds(jwtConfig.accessTokenValidityInSeconds)),
                "refreshTokens" to defaultCacheConfig.entryTtl(Duration.ofSeconds(jwtConfig.refreshTokenValidityInSeconds)),
            )

        return RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(defaultCacheConfig)
            .withInitialCacheConfigurations(configMap)
            .transactionAware()
            .build()
    }
}
