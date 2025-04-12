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
    type: String? = null,
    @Schema(
        description = "오류 발생 위치",
        example = "/api/users/v1/register",
        format = "uri",
    )
    instance: String? = null,
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
        type?.takeIf { it.isNotEmpty() }?.let { this.type = URI(it) }
        instance?.takeIf { it.isNotEmpty() }?.let { this.instance = URI(it) }
        title.takeIf { it.isNotEmpty() }?.let { this.title = it }

        setProperty("timestamp", timestamp)
        setProperty("traceId", traceId)
    }

    @Schema(description = "필드 오류 상세 정보")
    data class FieldErrorDetail(
        @Schema(description = "오류가 발생한 필드 이름", example = "email")
        val field: String,
        @Schema(description = "오류 메시지", example = "유효한 이메일 형식이 아닙니다.")
        val message: String,
    )

    /** Map<String,String> 또는 List<FieldErrorDetail> 모두 허용 */
    fun addFieldErrors(errors: Any): ErrorDetail {
        setProperty("fieldErrors", errors)
        return this
    }

    fun setPath(path: String): ErrorDetail {
        instance = URI.create(path)
        return this
    }

    companion object {
        /** 일반 오류 */
        fun of(
            status: HttpStatus,
            detail: String,
            path: String? = null,
        ): ErrorDetail =
            ErrorDetail(
                status = status.value(),
                detail = detail,
                type = ErrorConstants.getTypeURIForStatus(status).toString(),
                instance = path,
                title = status.reasonPhrase,
            )

        /** 입력값 검증 오류 */
        fun validation(
            status: HttpStatus,
            detail: String,
            fieldErrors: Any, // Map<String,String> | List<FieldErrorDetail>
            path: String? = null,
        ): ErrorDetail =
            of(status, detail, path).addFieldErrors(fieldErrors)
    }
}
