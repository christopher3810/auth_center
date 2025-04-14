package com.auth.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "spring.data.redis")
data class RedisProperties
    @ConstructorBinding
    constructor(
        val host: String,
        val port: Int,
        val password: String = "",
        val timeout: Long = 0,
        val database: Int = 0,
    )
