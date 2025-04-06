package com.auth.api.security

import com.auth.application.auth.service.TokenAppService
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExtractionException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

private val kLogger = KotlinLogging.logger {}

/**
 * 유효한 경우 SecurityContext 에 인증 정보를 설정.
 */
class JwtAuthenticationFilter(
    private val tokenAppService: TokenAppService,
) : OncePerRequestFilter() {
    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val AUTHORIZATION = "Authorization"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            // 토큰 추출 및 처리
            val token = extractJwtToken(request)
            if (token != null) {
                processTokenAuthentication(token)
            }

            // 필터 체인 계속 진행
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            // FilterChainExceptionHandler가 모든 예외를 처리할 것이므로 다시 throw
            throw ex
        }
    }

    /**
     * 토큰 인증 처리
     *
     * @param token JWT 토큰
     * @throws InvalidTokenException 토큰이 유효하지 않은 경우
     */
    private fun processTokenAuthentication(token: String) {
        if (!tokenAppService.validateToken(token)) {
            kLogger.warn { "유효하지 않은 JWT 토큰" }
            throw InvalidTokenException("유효하지 않은 JWT 토큰입니다.")
        }

        try {
            val userInfo = tokenAppService.getUserInfoFromToken(token)
            SecurityUtils.setupAuthentication(userInfo)
        } catch (ex: Exception) {
            kLogger.error(ex) { "JWT 토큰에서 사용자 정보 추출 실패" }
            throw TokenExtractionException("토큰에서 사용자 정보를 추출할 수 없습니다.")
        }
    }

    /**
     * Authorization 헤더에서 Bearer 토큰을 추출.
     *
     * @param request HTTP 요청
     * @return 추출된 JWT 토큰 또는 null
     */
    private fun extractJwtToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(AUTHORIZATION)

        return if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            authHeader.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}
