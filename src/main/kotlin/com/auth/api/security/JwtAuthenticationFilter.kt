package com.auth.api.security

import com.auth.application.auth.dto.UserTokenInfo
import com.auth.application.auth.service.TokenAppService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

/**
 * JWT 인증 필터
 * 
 * 요청 헤더에서 JWT 토큰을 추출하고 해당 토큰의 유효성을 검증한 후,
 * 유효한 경우 SecurityContext 에 인증 정보를 설정.
 */
class JwtAuthenticationFilter(
    private val tokenAppService: TokenAppService
) : OncePerRequestFilter() {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val AUTHORIZATION = "Authorization"
    }

    override fun doFilterInternal(
        request: HttpServletRequest, 
        response: HttpServletResponse, 
        filterChain: FilterChain
    ) {
        extractJwtToken(request)
            ?.let { token -> processTokenAuthentication(token) }

        filterChain.doFilter(request, response)
    }

    private fun processTokenAuthentication(token: String) = runCatching {
        if (tokenAppService.validateToken(token)) {
            val userInfo = tokenAppService.getUserInfoFromToken(token)
            SecurityUtils.setupAuthentication(userInfo)
        }
    }.onFailure { ex ->
        logger.warn { "JWT 인증 처리 중 오류 발생: ${ex.message}" }
    }
    
    /**
     * Authorization 헤더에서 Bearer 토큰을 추출.
     */
    private fun extractJwtToken(request: HttpServletRequest): String? =
        request.getHeader(AUTHORIZATION)
            ?.takeIf { tokenHeader : String -> tokenHeader.startsWith(BEARER_PREFIX) }
            ?.substring(BEARER_PREFIX.length)
}