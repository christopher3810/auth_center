package com.auth.api.rest.exception

/**
 * API 오류 응답의 JSON 예제를 중앙 관리하는 객체
 * OpenAPI 문서화와 각 핸들러의 응답 예제를 위해 사용됩니다.
 */
object ErrorExamples {
    // 기본 타임스탬프와 추적 ID (예제용)
    private const val EXAMPLE_TIMESTAMP = 1715117415000L
    private const val EXAMPLE_TRACE_ID = "e4b0d8c3-1234-5678-abcd-ef1234567890"

    // 유효성 검증 오류 예제
    const val VALIDATION_ERROR_EXAMPLE = """
    {
      "type": "https://api.example.com/errors/validation",
      "title": "Bad Request",
      "status": 400,
      "detail": "입력값 검증에 실패했습니다.",
      "instance": "/api/users/v1/register",
      "timestamp": $EXAMPLE_TIMESTAMP,
      "traceId": "$EXAMPLE_TRACE_ID",
      "fieldErrors": [
        {
          "field": "username",
          "message": "사용자명은 필수 입력값입니다."
        },
        {
          "field": "email",
          "message": "유효한 이메일 형식이 아닙니다."
        },
        {
          "field": "password",
          "message": "비밀번호는 8자 이상이어야 합니다."
        }
      ]
    }
    """

    // 인증 실패 오류 예제
    const val AUTHENTICATION_ERROR_EXAMPLE = """
    {
      "type": "https://api.example.com/errors/authentication",
      "title": "Unauthorized",
      "status": 401,
      "detail": "인증에 실패했습니다.",
      "instance": "/api/users/v1/info",
      "timestamp": $EXAMPLE_TIMESTAMP,
      "traceId": "$EXAMPLE_TRACE_ID"
    }
    """

    // 권한 부족 오류 예제
    const val FORBIDDEN_ERROR_EXAMPLE = """
    {
      "type": "https://api.example.com/errors/authorization",
      "title": "Forbidden",
      "status": 403,
      "detail": "해당 리소스에 접근할 권한이 없습니다.",
      "instance": "/api/admin/users",
      "timestamp": $EXAMPLE_TIMESTAMP,
      "traceId": "$EXAMPLE_TRACE_ID"
    }
    """

    // 리소스 없음 오류 예제
    const val NOT_FOUND_ERROR_EXAMPLE = """
    {
      "type": "https://api.example.com/errors/resource",
      "title": "Not Found",
      "status": 404,
      "detail": "요청한 리소스를 찾을 수 없습니다.",
      "instance": "/api/users/999",
      "timestamp": $EXAMPLE_TIMESTAMP,
      "traceId": "$EXAMPLE_TRACE_ID"
    }
    """

    // 서버 오류 예제
    const val SERVER_ERROR_EXAMPLE = """
    {
      "type": "https://api.example.com/errors/server",
      "title": "Internal Server Error",
      "status": 500,
      "detail": "서버 내부 오류가 발생했습니다.",
      "instance": "/api/users/v1/register",
      "timestamp": $EXAMPLE_TIMESTAMP,
      "traceId": "$EXAMPLE_TRACE_ID"
    }
    """

    // 사용자 관련 오류 예제
    const val USER_NOT_FOUND_EXAMPLE = """
    {
      "type": "https://api.example.com/errors/resource",
      "title": "Not Found",
      "status": 404,
      "detail": "요청한 사용자를 찾을 수 없습니다.",
      "instance": "/api/users/v1/12345",
      "timestamp": $EXAMPLE_TIMESTAMP,
      "traceId": "$EXAMPLE_TRACE_ID"
    }
    """

    const val USER_ALREADY_EXISTS_EXAMPLE = """
    {
      "type": "https://api.example.com/errors/resource",
      "title": "Conflict",
      "status": 409,
      "detail": "이미 존재하는 사용자입니다.",
      "instance": "/api/users/v1/register",
      "timestamp": $EXAMPLE_TIMESTAMP,
      "traceId": "$EXAMPLE_TRACE_ID"
    }
    """

    // 인증 관련 오류 예제
    const val INVALID_AUTH_HEADER_EXAMPLE = """
    {
      "type": "https://api.example.com/errors/authentication",
      "title": "Unauthorized",
      "status": 401,
      "detail": "유효하지 않은 Authorization 헤더입니다.",
      "instance": "/api/users/v1/info",
      "timestamp": $EXAMPLE_TIMESTAMP,
      "traceId": "$EXAMPLE_TRACE_ID"
    }
    """

    const val INVALID_CREDENTIALS_EXAMPLE = """
    {
      "type": "https://api.example.com/errors/authentication",
      "title": "Unauthorized",
      "status": 401,
      "detail": "아이디 또는 비밀번호가 맞지 않습니다.",
      "instance": "/api/auth/v1/login",
      "timestamp": $EXAMPLE_TIMESTAMP,
      "traceId": "$EXAMPLE_TRACE_ID"
    }
    """

    const val INVALID_TOKEN_EXAMPLE = """
    {
      "type": "https://api.example.com/errors/authentication",
      "title": "Unauthorized",
      "status": 401,
      "detail": "유효하지 않은 토큰입니다.",
      "instance": "/api/users/v1/info",
      "timestamp": $EXAMPLE_TIMESTAMP,
      "traceId": "$EXAMPLE_TRACE_ID"
    }
    """
}
