package com.auth.api.rest.exception

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.net.URI

object ErrorConstants {
    // 미디어 타입
    const val PROBLEM_JSON_MEDIA_TYPE = MediaType.APPLICATION_PROBLEM_JSON_VALUE

    // 베이스 오류 URI
    const val BASE_TYPE_URI = "https://api.example.com/errors"

    // 오류 유형 경로 (URI의 일부가 됨)
    private const val VALIDATION_TYPE_PATH = "validation"
    private const val AUTHENTICATION_TYPE_PATH = "authentication"
    private const val AUTHORIZATION_TYPE_PATH = "authorization"
    private const val RESOURCE_TYPE_PATH = "resource"
    private const val SERVER_TYPE_PATH = "server"

    // 오류 유형 URI - 공개 접근을 위해 확장 함수 사용
    val VALIDATION_TYPE_URI: URI = URI.create("$BASE_TYPE_URI/$VALIDATION_TYPE_PATH")
    val AUTHENTICATION_TYPE_URI: URI = URI.create("$BASE_TYPE_URI/$AUTHENTICATION_TYPE_PATH")
    val AUTHORIZATION_TYPE_URI: URI = URI.create("$BASE_TYPE_URI/$AUTHORIZATION_TYPE_PATH")
    val RESOURCE_TYPE_URI: URI = URI.create("$BASE_TYPE_URI/$RESOURCE_TYPE_PATH")
    val SERVER_TYPE_URI: URI = URI.create("$BASE_TYPE_URI/$SERVER_TYPE_PATH")

    // HTTP 상태 코드
    val BAD_REQUEST_STATUS: HttpStatus = HttpStatus.BAD_REQUEST
    val UNAUTHORIZED_STATUS: HttpStatus = HttpStatus.UNAUTHORIZED
    val FORBIDDEN_STATUS: HttpStatus = HttpStatus.FORBIDDEN
    val NOT_FOUND_STATUS: HttpStatus = HttpStatus.NOT_FOUND
    val CONFLICT_STATUS: HttpStatus = HttpStatus.CONFLICT
    val SERVER_ERROR_STATUS: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR

    // 기본 오류 메시지
    const val VALIDATION_ERROR_MESSAGE = "입력값 검증에 실패했습니다."
    const val FIELD_ERROR_DEFAULT_MESSAGE = "유효하지 않은 값입니다."
    const val UNAUTHORIZED_MESSAGE = "인증이 필요합니다. 로그인 후 다시 시도하세요."
    const val FORBIDDEN_MESSAGE = "해당 리소스에 접근할 권한이 없습니다."
    const val SERVER_ERROR_MESSAGE = "서버 내부 오류가 발생했습니다."
    const val INTERNAL_SERVER_ERROR_MESSAGE = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
    const val AUTHENTICATION_FAILED_MESSAGE = "인증에 실패했습니다."
    const val ACCESS_DENIED_MESSAGE = "해당 리소스에 접근할 권한이 없습니다."

    // 인증 관련 메시지
    const val INVALID_AUTH_HEADER_MESSAGE = "유효하지 않은 Authorization 헤더입니다."
    const val INVALID_CREDENTIALS_MESSAGE = "아이디 또는 비밀번호가 맞지 않습니다."
    const val INVALID_TOKEN_MESSAGE = "유효하지 않은 토큰입니다."
    const val TOKEN_EXTRACTION_ERROR_MESSAGE = "토큰에서 사용자 정보를 추출할 수 없습니다."

    // 리소스 관련 메시지
    const val USER_NOT_FOUND_MESSAGE = "사용자를 찾을 수 없습니다."
    const val USER_ALREADY_EXISTS_MESSAGE = "이미 존재하는 사용자입니다."
    const val RESOURCE_NOT_FOUND_MESSAGE = "요청한 리소스를 찾을 수 없습니다."
    const val ELEMENT_NOT_FOUND_MESSAGE = "요청한 요소를 찾을 수 없습니다."

    // 로그 메시지
    const val DEFAULT_ERROR_LOG = "오류 발생"
    const val SERVER_ERROR_LOG = "서버 내부 오류"
    const val INTERNAL_SERVER_ERROR_LOG = "처리되지 않은 서버 내부 오류"
    const val AUTHENTICATION_FAILURE_LOG = "인증 실패"
    const val AUTHENTICATION_FAILED_LOG = "인증 실패 - 일반"
    const val ACCESS_DENIED_LOG = "접근 권한 없음"
    const val TOKEN_ERROR_LOG = "토큰 오류"
    const val AUTH_HEADER_ERROR_LOG = "인증 실패 - 유효하지 않은 Authorization 헤더"
    const val INVALID_CREDENTIALS_LOG = "인증 실패 - 유효하지 않은 인증 정보"
    const val INVALID_TOKEN_LOG = "유효하지 않은 토큰"
    const val TOKEN_PARSING_ERROR_LOG = "토큰 파싱/추출 중 오류"
    const val VALIDATION_ERROR_LOG = "유효성 검증 실패"
    const val USER_NOT_FOUND_LOG = "사용자를 찾을 수 없음"
    const val USER_ALREADY_EXISTS_LOG = "이미 존재하는 사용자"
    const val ELEMENT_NOT_FOUND_LOG = "요소를 찾을 수 없음"
    const val UNCAUGHT_EXCEPTION_LOG = "처리되지 않은 예외 발생"
    const val FILTER_CHAIN_ERROR_LOG = "필터 체인에서 예외 발생"

    /**
     * 상태 코드에 따른 타입 URI를 생성합니다.
     */
    fun getTypeURIForStatus(status: HttpStatus): URI =
        when (status) {
            BAD_REQUEST_STATUS -> VALIDATION_TYPE_URI
            UNAUTHORIZED_STATUS -> AUTHENTICATION_TYPE_URI
            FORBIDDEN_STATUS -> AUTHORIZATION_TYPE_URI
            NOT_FOUND_STATUS -> RESOURCE_TYPE_URI
            SERVER_ERROR_STATUS -> SERVER_TYPE_URI
            else -> URI.create("$BASE_TYPE_URI/${status.value()}")
        }
}

fun URI.asString(): String = this.toString()

/**
 * HttpStatus 연관된 오류 타입 URI 반환
 */
fun HttpStatus.getErrorTypeURI(): URI = ErrorConstants.getTypeURIForStatus(this)

/**
 * HttpStatus 연관된 오류 타입 URI 문자열로 반환
 */
fun HttpStatus.getErrorTypeURIString(): String = ErrorConstants.getTypeURIForStatus(this).toString()
