package com.auth.api.security

import com.auth.application.auth.dto.UserTokenInfo
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Spring Security 관련 유틸리티 기능을 제공하는 객체
 */
object SecurityUtils {

    /**
     * 관리자 권한이 필요한 경우를 위한 인증 설정
     */
    fun setupAdminAuthentication(userInfo: UserTokenInfo) {
        val adminRoles = userInfo.roles + "ADMIN"
        val authorities = toRoleAuthorities(adminRoles)

        applyAuthentication(createAuthentication(userInfo, authorities))
    }

    /**
     * 읽기 전용 권한으로 인증을 설정.
     */
    fun setupReadOnlyAuthentication(userInfo: UserTokenInfo) {
        val readOnlyRoles = setOf("READER")
        val authorities = toRoleAuthorities(readOnlyRoles)

        applyAuthentication(createAuthentication(userInfo, authorities))
    }

    /**
     * 커스텀 권한 목록으로 인증을 설정.
     */
    fun setupAuthenticationWithRoles(userInfo: UserTokenInfo, customRoles: Set<String>) {
        val authorities = toRoleAuthorities(customRoles)
        
        applyAuthentication(createAuthentication(userInfo, authorities))
    }

    /**
     * 기본 인증 설정 - 사용자의 기존 역할 사용
     */
    fun setupAuthentication(userInfo: UserTokenInfo) {
        val authorities = toRoleAuthorities(userInfo.roles)
        
        applyAuthentication(createAuthentication(userInfo, authorities))
    }

    /**
     * 문자열 컬렉션을 Spring Security 권한 객체 목록으로 변환
     */
    private fun toRoleAuthorities(roles: Collection<String>): List<SimpleGrantedAuthority> =
        roles.map { toRoleAuthority(it) }

    /**
     * 역할 문자열을 Spring Security 권한 객체로 변환
     *
     * ROLE_ 접두사가 없는 경우 자동으로 추가.
     * 예: "ADMIN" -> "ROLE_ADMIN", "ROLE_USER" -> "ROLE_USER"
     */
    private fun toRoleAuthority(role: String): SimpleGrantedAuthority =
        SimpleGrantedAuthority(if (role.startsWith("ROLE_")) role else "ROLE_$role")

    /**
     * Authentication 객체 생성
     */
    private fun createAuthentication(userInfo: UserTokenInfo, authorities: List<GrantedAuthority>): UsernamePasswordAuthenticationToken =
        UsernamePasswordAuthenticationToken(
            userInfo,          // principal - UserTokenInfo 객체
            null,              // credentials - JWT 인증에서는 불필요
            authorities        // 사용자 권한
        )

    /**
     * Authentication 객체를 현재 SecurityContext에 적용
     */
    private fun applyAuthentication(authentication: UsernamePasswordAuthenticationToken) {
        SecurityContextHolder.getContext().authentication = authentication
    }

    /**
     * 현재 스레드의 SecurityContext를 완전히 초기화.
     */
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    /**
     * 현재 SecurityContext가 인증된 상태인지 확인
     *
     * @return 현재 컨텍스트가 인증된 상태이면 true, 그렇지 않으면 false
     */
    fun isAuthenticated(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated
    }
} 