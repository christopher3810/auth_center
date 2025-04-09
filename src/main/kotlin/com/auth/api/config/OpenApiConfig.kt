package com.auth.api.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Auth_Center API")
                    .version("1.0.0")
                    .description(
                        """
                        사용자 인증, 회원가입, 로그인, 토큰 관리 등의 인증 관련 API를 제공합니다.
                        
                        주요 기능
                        * 회원가입: 새로운 사용자 등록
                        * 로그인: 이메일/사용자명과 비밀번호를 통한 로그인
                        * 토큰 관리: JWT 액세스 토큰 및 리프레시 토큰 발급/갱신
                        * 사용자 정보: 로그인된 사용자의 프로필 정보 조회
                        
                        오류 응답
                        모든 오류는 RFC 7807 Problem Details 형식으로 반환됩니다.
                        """,
                    ).contact(
                        Contact()
                            .name("Auth_Center Creator")
                            .email("lonesome661@naver.com"),
                    ).license(
                        License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0.html"),
                    ),
            ).servers(
                listOf(
                    Server()
                        .url("/")
                        .description("Current server"),
                ),
            ).components(
                Components()
                    .addSecuritySchemes(
                        "bearer-jwt",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT 인증을 위한 Bearer 토큰을 입력하세요."),
                    ),
            ).addSecurityItem(SecurityRequirement().addList("bearer-jwt"))
}
