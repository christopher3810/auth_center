package com.auth.api.config

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.ErrorExamples
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod

@Configuration
class OpenApiConfig {
    // 미디어 타입 상수 - RFC 7807 표준 미디어 타입


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
            )
            .components(
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
    /**
     * 전역 공통 응답 커스터마이저
     * 모든 API 엔드포인트에 공통 응답을 자동으로 추가.
     */
    @Bean
    fun globalOpenApiCustomizer(): GlobalOpenApiCustomizer =
        GlobalOpenApiCustomizer { openApi ->
            // 기존 컴포넌트가 없으면 생성
            if (openApi.components == null) {
                openApi.components = Components()
            }

            // 필드 오류 상세 스키마 정의
            val fieldErrorDetailSchema = createFieldErrorSchema()
            openApi.components.addSchemas("FieldErrorDetail", fieldErrorDetailSchema)

            // ErrorDetail 스키마 정의 (문제 세부 정보 표준)
            val errorDetailSchema = createErrorDetailSchema()
            openApi.components.addSchemas("ErrorDetail", errorDetailSchema)

            // 공통 응답 정의
            val responses = createCommonResponses()
            responses.forEach { (code, response) ->
                openApi.components.addResponses(code, response)
            }
        }

    /**
     * 개별 API 작업에 공통 파라미터나 응답을 추가하는 커스터마이저
     */
    @Bean
    fun operationCustomizer(): OperationCustomizer =
        OperationCustomizer { operation: Operation, _: HandlerMethod ->
            // 모든 API 메서드에 공통 응답 추가
            if (operation.responses == null) {
                operation.responses = ApiResponses()
            }

            // 모든 메서드에 공통 오류 응답 추가
            addCommonErrorResponses(operation)

            // Authorization 헤더가 필요한 메서드에 공통 헤더 파라미터 추가
            addAuthHeaderIfNeeded(operation)

            operation
        }

    /**
     * ErrorDetail 스키마 생성
     */
    private fun createErrorDetailSchema(): Schema<*> {
        val schema =
            Schema<ErrorDetail>()
                .type("object")
                .description("RFC 7807 Problem Details 형식의 오류 상세 정보")

        schema.addProperty(
            "type",
            Schema<String>()
                .type("string")
                .format("uri")
                .description("문제 유형을 식별하는 URI 참조")
                .example("https://api.example.com/errors/validation"),
        )

        schema.addProperty(
            "title",
            Schema<String>()
                .type("string")
                .description("문제 유형에 대한 간략한 제목")
                .example("Bad Request"),
        )

        schema.addProperty(
            "status",
            Schema<Int>()
                .type("integer")
                .description("HTTP 상태 코드")
                .example(400),
        )

        schema.addProperty(
            "detail",
            Schema<String>()
                .type("string")
                .description("이 특정 문제 인스턴스에 대한 상세 설명")
                .example("입력값 검증에 실패했습니다."),
        )

        schema.addProperty(
            "instance",
            Schema<String>()
                .type("string")
                .format("uri")
                .description("문제가 발생한 특정 URI")
                .example("/api/users/v1/register"),
        )

        schema.addProperty(
            "timestamp",
            Schema<Long>()
                .type("integer")
                .format("int64")
                .description("오류 발생 시간 (밀리초 단위 타임스탬프)")
                .example(1715117415000),
        )

        // TODO : gateway TraceId 와 연동 설정
        schema.addProperty(
            "traceId",
            Schema<String>()
                .type("string")
                .description("오류 추적을 위한 고유 ID")
                .example("e4b0d8c3-1234-5678-abcd-ef1234567890"),
        ) // 임시값

        // 필드 에러 배열 추가
        val fieldErrorArraySchema =
            ArraySchema()
                .items(Schema<Any>().type("object").name("FieldErrorDetail"))
                .description("유효성 검증 오류가 있는 필드와 메시지 목록")

        schema.addProperty("fieldErrors", fieldErrorArraySchema)

        return schema
    }

    /**
     * FieldError 스키마 생성 (직접 정의)
     */
    private fun createFieldErrorSchema(): Schema<*> {
        val schema =
            Schema<Any>()
                .type("object")
                .description("필드 오류 상세 정보")

        schema.addProperty(
            "field",
            Schema<String>()
                .type("string")
                .description("오류가 발생한 필드 이름")
                .example("email"),
        )

        schema.addProperty(
            "message",
            Schema<String>()
                .type("string")
                .description("오류 메시지")
                .example("유효한 이메일 형식이 아닙니다."),
        )

        return schema
    }

    /**
     * 공통 오류 응답 생성
     */
    private fun createCommonResponses(): Map<String, ApiResponse> {
        // 각 응답 코드와 예제를 매핑
        val responseExamples =
            mapOf(
                "400" to Pair("입력값 검증 오류", ErrorExamples.VALIDATION_ERROR_EXAMPLE),
                "401" to Pair("인증 실패", ErrorExamples.AUTHENTICATION_ERROR_EXAMPLE),
                "403" to Pair("권한 부족", ErrorExamples.FORBIDDEN_ERROR_EXAMPLE),
                "404" to Pair("리소스를 찾을 수 없음", ErrorExamples.NOT_FOUND_ERROR_EXAMPLE),
                "500" to Pair("서버 오류", ErrorExamples.SERVER_ERROR_EXAMPLE),
            )

        // 응답 코드별로 ApiResponse 객체 생성
        return responseExamples.mapValues { (_, descriptionAndExample) ->
            createApiResponseWithExample(descriptionAndExample.first, descriptionAndExample.second)
        }
    }

    /**
     * 설명과 예제로 ApiResponse 객체 생성
     */
    private fun createApiResponseWithExample(
        description: String,
        exampleJson: String,
    ): ApiResponse =
        ApiResponse()
            .description(description)
            .content(
                Content().addMediaType(
                    ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
                    createMediaTypeWithExample(exampleJson),
                ),
            )

    /**
     * 오류 응답 예제를 포함한 MediaType 생성
     */
    private fun createMediaTypeWithExample(exampleValue: String): MediaType {
        val schema =
            Schema<ErrorDetail>()
                .type("object")
                .name("ErrorDetail")

        return MediaType()
            .schema(schema)
            .addExamples("default", Example().value(exampleValue))
    }

    /**
     * 작업에 공통 오류 응답 추가
     */
    private fun addCommonErrorResponses(operation: Operation) {
        // 공통 오류 코드
        val errorCodes = listOf("400", "401", "403", "404", "500")

        errorCodes.forEach { code ->
            // 이미 정의된 응답이 없는 경우에만 추가
            if (!operation.responses.containsKey(code)) {
                val response =
                    ApiResponse()
                        .description("ErrorDetail")
                        .content(
                            Content().addMediaType(
                                ErrorConstants.PROBLEM_JSON_MEDIA_TYPE,
                                MediaType().schema(
                                    Schema<ErrorDetail>()
                                        .type("object")
                                        .name("ErrorDetail"),
                                ),
                            ),
                        )
                operation.responses.addApiResponse(code, response)
            }
        }
    }

    /**
     * 필요한 경우 인증 헤더 파라미터 추가
     */
    private fun addAuthHeaderIfNeeded(operation: Operation) {
        if (operation.security != null && operation.security.isNotEmpty()) {
            val authParameter =
                Parameter()
                    .name("Authorization")
                    .description("JWT 액세스 토큰")
                    .`in`(ParameterIn.HEADER.toString())
                    .required(true)
                    .schema(Schema<String>().type("string"))
                    .example("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")

            if (operation.parameters == null) {
                operation.parameters = mutableListOf()
            }

            // 이미 Authorization 파라미터가 있는지 확인 후 없으면 추가
            val hasAuthParam =
                operation.parameters
                    .any { it.name == "Authorization" && it.`in` == ParameterIn.HEADER.toString() }

            if (!hasAuthParam) {
                operation.parameters.add(authParameter)
            }
        }
    }
}
