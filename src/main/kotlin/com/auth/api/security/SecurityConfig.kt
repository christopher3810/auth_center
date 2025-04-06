package com.auth.api.security

import com.auth.api.rest.exception.handler.FilterChainExceptionHandler
import com.auth.api.rest.exception.handler.SecurityAuthExceptionHandler
import com.auth.application.auth.service.TokenAppService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val tokenAppService: TokenAppService,
    private val securityAuthExceptionHandler: SecurityAuthExceptionHandler,
    private val filterChainExceptionHandler: FilterChainExceptionHandler,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(
        http: HttpSecurity,
        @Value("\${security.permit-all-patterns:}") permitAllPatterns: List<String>,
    ): SecurityFilterChain {
        // CSRF 비활성화 및 세션 관리 설정
        http
            .csrf { csrfConfigurer: CsrfConfigurer<HttpSecurity> -> csrfConfigurer.disable() }
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .sessionManagement { sessionConfigurer: SessionManagementConfigurer<HttpSecurity> ->
                sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // 인증 및 권한 예외 처리 핸들러 설정
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(securityAuthExceptionHandler)
                    .accessDeniedHandler(securityAuthExceptionHandler)
            }
            // URL 기반 접근 제어 설정
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(*permitAllPatterns.toTypedArray())
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }

        // 필터 체인 예외 처리 필터 추가 (가장 먼저 실행)
        http.addFilterBefore(
            filterChainExceptionHandler,
            UsernamePasswordAuthenticationFilter::class.java,
        )

        // UsernamePasswordAuthenticationFilter 앞단에서 JWT 필터를 실행하여 인증 처리
        http.addFilterBefore(
            JwtAuthenticationFilter(tokenAppService),
            UsernamePasswordAuthenticationFilter::class.java,
        )

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration =
            CorsConfiguration().apply {
                allowedOrigins = listOf("*")
                allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                allowedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With")
                exposedHeaders = listOf("Authorization")
                maxAge = 3600L
            }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
