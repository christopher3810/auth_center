package com.auth.api.security

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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val tokenAppService: TokenAppService
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * Spring Security 6 에서는 WebSecurityConfigurerAdapter가 deprecated 되었으므로,
     * SecurityFilterChain을 Bean 으로 등록하는 방법을 사용.
     */
    @Bean
    fun filterChain(
        http: HttpSecurity,
        @Value("\${security.permit-all-patterns:}") permitAllPatterns: List<String>
    ): SecurityFilterChain {
        http
            .csrf { csrfConfigurer : CsrfConfigurer<HttpSecurity> -> csrfConfigurer.disable() }
            .sessionManagement { sessionConfigurer : SessionManagementConfigurer<HttpSecurity> ->
                sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(*permitAllPatterns.toTypedArray()).permitAll()
                    .anyRequest().authenticated()
            }
            // UsernamePasswordAuthenticationFilter 앞단에서 JWT 필터를 실행하여 인증 처리
            .addFilterBefore(
                JwtAuthenticationFilter(tokenAppService),
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}