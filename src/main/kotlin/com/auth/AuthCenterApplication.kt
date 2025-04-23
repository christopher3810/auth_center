package com.auth

import com.auth.infrastructure.cache.CacheProperties
import com.auth.infrastructure.config.JwtConfig
import com.auth.infrastructure.config.RedisProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtConfig::class, RedisProperties::class, CacheProperties::class)
class AuthCenterApplication

fun main(args: Array<String>) {
    runApplication<AuthCenterApplication>(*args)
}
