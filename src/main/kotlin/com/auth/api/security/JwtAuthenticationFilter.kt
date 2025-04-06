package com.auth.api.security

import com.auth.application.auth.service.TokenAppService
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExtractionException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.util.AntPathMatcher
import org.springframework.util.PathMatcher
import org.springframework.web.filter.OncePerRequestFilter

private val kLogger = KotlinLogging.logger {}

/**
 * JWT 인증 필터
 * 요청 헤더에서 JWT 토큰을 추출하고 검증.
 * 유효한 경우 SecurityContext 에 인증 정보를 설정.
 */
class JwtAuthenticationFilter(
    private val tokenAppService: TokenAppService,
    private val permitAllPatterns: List<String>,
) : OncePerRequestFilter() {
    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val AUTHORIZATION = "Authorization"
    }

    private val pathMatcher: PathMatcher = AntPathMatcher()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        permitAllPatterns.any { pattern ->
            pathMatcher.match(pattern, request.requestURI)
        }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        runCatching {
            extractJwtToken(request)?.let { token ->
                processTokenAuthentication(token)
            }
        }.onFailure { ex ->
            // 토큰 관련 예외만 기록하고 계속 진행
            when (ex) {
                is InvalidTokenException,
                is TokenExtractionException,
                -> kLogger.warn(ex) { "JWT 토큰 처리 오류: ${ex.message}" }
                else -> throw ex // 다른 예외는 다시 throw
            }
        }

        filterChain.doFilter(request, response)
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

        runCatching {
            tokenAppService.getUserInfoFromToken(token)
        }.onSuccess { userInfo ->
            SecurityUtils.setupAuthentication(userInfo)
        }.onFailure { ex ->
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
    private fun extractJwtToken(request: HttpServletRequest): String? =
        request
            .getHeader(AUTHORIZATION)
            ?.takeIf {
                it.startsWith(BEARER_PREFIX)
            }?.substring(BEARER_PREFIX.length)
}
