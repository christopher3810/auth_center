package com.auth.api.security.annotation

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.security.access.prepost.PreAuthorize
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.Retention
import kotlin.annotation.Target

/**
 * 리소스 소유자 또는 관리자만 접근을 허용하는 어노테이션
 * 메서드에 @PathVariable Long userId가 있어야 함
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('ADMIN') or principal.id == #userId")
@SecurityRequirement(name = "bearer-jwt")
annotation class ResourceOwnerOrAdmin
