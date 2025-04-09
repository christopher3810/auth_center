package com.auth.api.security

import com.auth.api.rest.exception.handler.FilterChainExceptionHandler
import com.auth.api.rest.exception.handler.SecurityAuthExceptionHandler
import com.auth.application.auth.service.TokenAppService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
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
    private val securityProperties: SecurityProperties,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val permitAllPatterns = securityProperties.permitAllPatterns
        http
            .csrf { csrfConfigurer: CsrfConfigurer<HttpSecurity> -> csrfConfigurer.disable() }
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .sessionManagement { sessionConfigurer: SessionManagementConfigurer<HttpSecurity> ->
                sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(securityAuthExceptionHandler)
                    .accessDeniedHandler(securityAuthExceptionHandler)
            }.authorizeHttpRequests { auth ->
                auth.requestMatchers(*permitAllPatterns.toTypedArray()).permitAll()
                auth.anyRequest().authenticated()
            }

        // JWT 인증 필터 추가 - permitAllPatterns 전달
        http.addFilterBefore(
            JwtAuthenticationFilter(tokenAppService, permitAllPatterns),
            UsernamePasswordAuthenticationFilter::class.java,
        )

        // 필터 체인 예외 처리 필터는 등록하지 않음 - 별도 FilterRegistrationBean으로 관리

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource =
        UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration(
                "/**",
                CorsConfiguration().apply {
                    allowedOrigins = listOf("*")
                    allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                    allowedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With")
                    exposedHeaders = listOf("Authorization")
                    maxAge = 3600L
                },
            )
        }

    @Bean
    fun filterChainExceptionHandlerRegistration(
        filterChainExceptionHandler: FilterChainExceptionHandler,
        @Value("\${security.permit-all-patterns:}") permitAllPatterns: List<String>,
    ): FilterRegistrationBean<FilterChainExceptionHandler> =
        FilterRegistrationBean<FilterChainExceptionHandler>().apply {
            filter =
                FilterChainExceptionHandler(
                    filterChainExceptionHandler.objectMapper,
                    permitAllPatterns,
                )
            order = Ordered.HIGHEST_PRECEDENCE
            urlPatterns = listOf("/*")
            setEnabled(true)
        }
}
