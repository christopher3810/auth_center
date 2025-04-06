package com.auth.api.security.annotation

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.Retention
import kotlin.annotation.Target

/**
 * 인증된 사용자만 접근을 허용하는 어노테이션
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearer-jwt")
annotation class AuthenticatedOnly
