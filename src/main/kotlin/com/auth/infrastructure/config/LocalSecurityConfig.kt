package com.auth.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@Profile("local")
class LocalSecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { requests ->
                requests
                    // Swagger UI 및 OpenAPI 문서 관련 URL은 인증 없이 접근
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()
            }
            .httpBasic(withDefaults())
            .formLogin(withDefaults())
            // 테스트 및 개발 환경에서 CSRF 보호가 필요 없으면 비활성화 (상황에 따라 조정)
            .csrf { csrf -> csrf.disable() }
        return http.build()
    }
}