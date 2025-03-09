package com.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuthCenterApplication

fun main(args: Array<String>) {
    runApplication<AuthCenterApplication>(*args)
} 