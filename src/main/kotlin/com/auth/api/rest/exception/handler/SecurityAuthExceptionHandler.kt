package com.auth.api.rest.exception.handler

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.asString
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Spring Security 의 인증 및 권한 예외를 처리하는 핸들러
 */
@Component
class SecurityAuthExceptionHandler(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint,
    AccessDeniedHandler {
    /**
     * 인증되지 않은 요청 처리 (401 Unauthorized)
     */
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val errorDetail =
            ErrorDetail(
                type = ErrorConstants.AUTHENTICATION_TYPE_URI.asString(),
                title = ErrorConstants.UNAUTHORIZED_STATUS.reasonPhrase,
                status = ErrorConstants.UNAUTHORIZED_STATUS.value(),
                detail = authException.message ?: ErrorConstants.AUTHENTICATION_FAILED_MESSAGE,
                instance = request.requestURI,
                timestamp = System.currentTimeMillis(),
                traceId = UUID.randomUUID().toString(),
            )

        sendErrorResponse(response, errorDetail)
    }

    /**
     * 접근 권한 없음 처리 (403 Forbidden)
     */
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        val errorDetail =
            ErrorDetail(
                type = ErrorConstants.AUTHORIZATION_TYPE_URI.asString(),
                title = ErrorConstants.FORBIDDEN_STATUS.reasonPhrase,
                status = ErrorConstants.FORBIDDEN_STATUS.value(),
                detail = accessDeniedException.message ?: ErrorConstants.ACCESS_DENIED_MESSAGE,
                instance = request.requestURI,
                timestamp = System.currentTimeMillis(),
                traceId = UUID.randomUUID().toString(),
            )

        sendErrorResponse(response, errorDetail)
    }

    /**
     * 오류 응답을 JSON 으로 변환하여 클라이언트에 전송
     */
    private fun sendErrorResponse(
        response: HttpServletResponse,
        errorDetail: ErrorDetail,
    ) {
        response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        response.status = errorDetail.status
        objectMapper.writeValue(response.outputStream, errorDetail)
    }
}
