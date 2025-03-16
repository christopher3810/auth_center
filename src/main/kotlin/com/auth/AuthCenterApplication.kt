package com.auth

import com.auth.infrastructure.config.JwtConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtConfig::class)
class AuthCenterApplication

fun main(args: Array<String>) {
    runApplication<AuthCenterApplication>(*args)
} 