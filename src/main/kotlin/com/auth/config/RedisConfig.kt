package com.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * Redis 설정
 */
@Configuration
class RedisConfig {

    /**
     * ReactiveRedisTemplate 설정
     * 
     * 블랙리스트된 토큰 정보를 Redis에 저장하기 위한 템플릿
     */
    @Bean
    fun reactiveRedisTemplate(connectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, String> {
        val serializer = StringRedisSerializer()
        
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, String>()
            .key(serializer)
            .value(serializer)
            .hashKey(serializer)
            .hashValue(serializer)
            .build()
        
        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }
} 