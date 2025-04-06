package com.auth.api.security.annotation

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.Retention
import kotlin.annotation.Target

/**
 * 관리자(ADMIN) 역할이 있는 사용자만 접근을 허용하는 어노테이션
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
annotation class AdminOnly
