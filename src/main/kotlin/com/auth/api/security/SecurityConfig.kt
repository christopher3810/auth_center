package com.auth.api.security

import com.auth.application.auth.service.TokenAppService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val tokenAppService: TokenAppService,
    private val customUserDetailsService: CustomUserDetailsService
) {
    /**
     * 비밀번호 암호화를 위한 PasswordEncoder 등록
     * - BCryptPasswordEncoder는 Spring Security에서 권장
     */
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
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(*permitAllPatterns.toTypedArray()).permitAll()
                    .anyRequest().authenticated()
            }
            // UsernamePasswordAuthenticationFilter 앞단에서 JWT 필터를 실행하여 인증 처리
            .addFilterBefore(
                JwtAuthenticationFilter(tokenAppService, customUserDetailsService),
                UsernamePasswordAuthenticationFilter::class.java
            )

        return http.build()
    }
}