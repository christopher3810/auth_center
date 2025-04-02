package com.auth.infrastructure.web

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * 요청 헤더에서 사용자 정보를 추출하는 유틸리티 클래스
 */
object RequestHeaderUtils {
    private val logger = LoggerFactory.getLogger(RequestHeaderUtils::class.java)

    // API 게이트웨이에서 전달되는 헤더 상수
    const val USER_ID_HEADER = "X-User-ID"
    const val USER_EMAIL_HEADER = "X-User-Email"
    const val USER_ROLE_HEADER = "X-User-Role"
    const val REQUEST_SOURCE_HEADER = "X-Request-Source"

    // 요청 소스 상수
    const val ADMIN_PORTAL_SOURCE = "admin-portal"
    const val USER_APP_SOURCE = "user-app"

    // 역할 상수
    const val ROLE_ADMIN = "ROLE_ADMIN"

    // 사용자 정보 포맷 상수
    const val ADMIN_PREFIX = "admin:"
    const val USER_PREFIX = "user:"

    /**
     * 현재 요청의 사용자 정보를 추출합니다.
     * API 게이트웨이에서 전달되는 헤더 정보를 기반으로 사용자 정보를 반환합니다.
     * 사용자 정보가 없으면 null을 반환합니다.
     *
     * @return 사용자 정보 문자열 또는 null
     */
    fun getCurrentUserInfo(): String? {
        try {
            val request = getCurrentRequest() ?: return null

            // 요청 소스 및 사용자 정보 확인
            val requestSource = request.getHeader(REQUEST_SOURCE_HEADER)
            val userEmail = request.getHeader(USER_EMAIL_HEADER)
            val userId = request.getHeader(USER_ID_HEADER)
            val userRole = request.getHeader(USER_ROLE_HEADER)

            // 요청 소스와 사용자 정보에 따라 다른 형식 반환
            return when {
                // 관리자 포털에서의 요청
                ADMIN_PORTAL_SOURCE == requestSource && !userEmail.isNullOrBlank() ->
                    "$ADMIN_PREFIX$userEmail"

                // 일반 사용자 앱에서의 요청
                USER_APP_SOURCE == requestSource && !userEmail.isNullOrBlank() ->
                    "$USER_PREFIX$userEmail"

                // 관리자 역할인 경우
                ROLE_ADMIN == userRole && !userEmail.isNullOrBlank() ->
                    "$ADMIN_PREFIX$userEmail"

                // 이메일 정보가 있는 경우
                !userEmail.isNullOrBlank() ->
                    userEmail

                // 사용자 ID가 있는 경우
                !userId.isNullOrBlank() ->
                    "$USER_PREFIX$userId"

                // 정보가 없는 경우
                else -> null
            }
        } catch (e: Exception) {
            logger.warn("Failed to extract user info from request: ${e.message}")
            return null
        }
    }

    /**
     * 현재 HTTP 요청을 가져옵니다.
     *
     * @return 현재 HTTP 요청 또는 null
     */
    private fun getCurrentRequest(): HttpServletRequest? =
        try {
            val requestAttributes = RequestContextHolder.getRequestAttributes()
            if (requestAttributes is ServletRequestAttributes) {
                requestAttributes.request
            } else {
                null
            }
        } catch (e: Exception) {
            logger.warn("Failed to get current request: ${e.message}")
            null
        }
}
