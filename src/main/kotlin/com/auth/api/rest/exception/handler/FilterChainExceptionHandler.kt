package com.auth.api.rest.exception.handler

import com.auth.api.rest.exception.ErrorConstants
import com.auth.api.rest.exception.ErrorDetail
import com.auth.api.rest.exception.asString
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExtractionException
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

val kLogger = KotlinLogging.logger {}

/**
 * Spring Security 필터 체인에서 발생하는 예외를 처리하는 필터
 */
@Component
class FilterChainExceptionHandler(
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            handleException(ex, request, response)
        }
    }

    private fun handleException(
        ex: Exception,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val errorDetail =
            when (ex) {
                // 인증 예외
                is AuthenticationException -> {
                    kLogger.warn { "${ErrorConstants.AUTHENTICATION_FAILURE_LOG}: ${ex.message}" }
                    createErrorDetail(
                        errorType = ErrorConstants.AUTHENTICATION_TYPE_URI.asString(),
                        status = ErrorConstants.UNAUTHORIZED_STATUS.value(),
                        title = ErrorConstants.UNAUTHORIZED_STATUS.reasonPhrase,
                        detail = ex.message ?: ErrorConstants.UNAUTHORIZED_MESSAGE,
                        path = request.requestURI,
                    )
                }
                // 권한 예외
                is AccessDeniedException -> {
                    kLogger.warn { "${ErrorConstants.ACCESS_DENIED_LOG}: ${ex.message}" }
                    createErrorDetail(
                        errorType = ErrorConstants.AUTHORIZATION_TYPE_URI.asString(),
                        status = ErrorConstants.FORBIDDEN_STATUS.value(),
                        title = ErrorConstants.FORBIDDEN_STATUS.reasonPhrase,
                        detail = ex.message ?: ErrorConstants.FORBIDDEN_MESSAGE,
                        path = request.requestURI,
                    )
                }
                // 토큰 예외
                is InvalidTokenException, is TokenExtractionException -> {
                    kLogger.warn { "${ErrorConstants.TOKEN_ERROR_LOG}: ${ex.message}" }
                    createErrorDetail(
                        errorType = ErrorConstants.AUTHENTICATION_TYPE_URI.asString(),
                        status = ErrorConstants.UNAUTHORIZED_STATUS.value(),
                        title = ErrorConstants.UNAUTHORIZED_STATUS.reasonPhrase,
                        detail = ex.message ?: ErrorConstants.INVALID_TOKEN_MESSAGE,
                        path = request.requestURI,
                    )
                }
                // 기타 예외
                else -> {
                    kLogger.error(ex) { ErrorConstants.FILTER_CHAIN_ERROR_LOG }
                    createErrorDetail(
                        errorType = ErrorConstants.SERVER_TYPE_URI.asString(),
                        status = ErrorConstants.SERVER_ERROR_STATUS.value(),
                        title = ErrorConstants.SERVER_ERROR_STATUS.reasonPhrase,
                        detail = ErrorConstants.SERVER_ERROR_MESSAGE,
                        path = request.requestURI,
                    )
                }
            }

        // 응답 설정
        response.contentType = ErrorConstants.PROBLEM_JSON_MEDIA_TYPE
        response.status = errorDetail.status
        objectMapper.writeValue(response.outputStream, errorDetail)
    }

    private fun createErrorDetail(
        errorType: String,
        status: Int,
        title: String,
        detail: String,
        path: String,
    ): ErrorDetail =
        ErrorDetail(
            status = status,
            detail = detail,
            type = errorType,
            instance = path,
            timestamp = System.currentTimeMillis(),
            traceId = UUID.randomUUID().toString(),
            title = title,
        )
}
