package com.auth.api.rest.exception

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import java.net.URI
import java.util.UUID

@Schema(
    description = "API 오류 응답 객체 - RFC 7807 Problem Details 표준 확장",
    title = "ErrorDetail",
)
class ErrorDetail(
    @Schema(
        description = "HTTP 상태 코드",
        example = "400",
        implementation = Int::class,
    )
    status: Int,
    @Schema(
        description = "오류 상세 메시지",
        example = "입력값 검증에 실패했습니다.",
    )
    detail: String,
    @Schema(
        description = "오류 유형 URI",
        example = "https://api.example.com/errors/validation",
        format = "uri",
    )
    type: String = "",
    @Schema(
        description = "오류 발생 위치",
        example = "/api/users/v1/register",
        format = "uri",
    )
    instance: String = "",
    @Schema(
        description = "오류 발생 시간",
        example = "1715117415000",
        format = "int64",
    )
    val timestamp: Long = System.currentTimeMillis(),
    @Schema(
        description = "오류 추적 ID (로깅/디버깅용)",
        example = "e4b0d8c3-1234-5678-abcd-ef1234567890",
    )
    val traceId: String = UUID.randomUUID().toString(),
    @Schema(
        description = "오류 제목",
        example = "Bad Request",
    )
    title: String = "",
) : ProblemDetail(status) {
    init {
        this.detail = detail
        if (type.isNotEmpty()) this.type = URI.create(type)
        if (instance.isNotEmpty()) this.instance = URI.create(instance)
        if (title.isNotEmpty()) this.title = title
        this.setProperty("timestamp", timestamp)
        this.setProperty("traceId", traceId)
    }

    @Schema(
        description = "필드 오류 상세 정보",
    )
    data class FieldErrorDetail(
        @Schema(
            description = "오류가 발생한 필드 이름",
            example = "email",
        )
        val field: String,
        @Schema(
            description = "오류 메시지",
            example = "유효한 이메일 형식이 아닙니다.",
        )
        val message: String,
    )

    fun withFieldErrors(errors: Map<String, String>): ErrorDetail {
        this.setProperty("fieldErrors", errors)
        return this
    }

    fun withFieldErrorList(errors: List<FieldErrorDetail>): ErrorDetail {
        this.setProperty("fieldErrors", errors)
        return this
    }

    fun withPath(path: String): ErrorDetail {
        this.instance = URI.create(path)
        return this
    }

    companion object {
        /**
         * 기본 오류 응답 생성
         */
        fun forStatus(
            status: HttpStatus,
            detail: String,
            path: String? = null,
        ): ErrorDetail {
            fun createErrorDetail(statusParam: HttpStatus): ErrorDetail =
                ErrorDetail(
                    status = statusParam.value(),
                    detail = detail,
                    title = statusParam.reasonPhrase,
                )

            val errorDetail = createErrorDetail(status)

            errorDetail.type = ErrorConstants.getTypeURIForStatus(status)

            path?.let { pathValue ->
                errorDetail.instance = URI.create(pathValue)
            }

            return errorDetail
        }

        fun forValidationError(
            status: HttpStatus,
            detail: String,
            fieldErrors: Map<String, String>,
            path: String? = null,
        ): ErrorDetail {
            val error = forStatus(status, detail, path)
            return error.withFieldErrors(fieldErrors)
        }

        fun forValidationError(
            status: HttpStatus,
            detail: String,
            fieldErrors: List<FieldErrorDetail>,
            path: String? = null,
        ): ErrorDetail {
            val error = forStatus(status, detail, path)
            return error.withFieldErrorList(fieldErrors)
        }
    }
}
