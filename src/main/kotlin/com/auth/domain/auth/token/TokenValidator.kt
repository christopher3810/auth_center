package com.auth.domain.auth.token
import io.jsonwebtoken.Claims

/**
 * 토큰 검증을 담당하는 도메인 서비스 인터페이스
 */
interface TokenValidator {
    /**
     * JWT 토큰을 파싱하여 Claims를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 Claims
     * @throws TokenException 토큰이 유효하지 않거나 필수 클레임이 존재하지 않을 경우
     */
    fun getClaims(token: String): Claims

    /**
     * 토큰의 유효성을 검증합니다.
     * Claims 추출 시 예외가 발생하지 않으면 유효한 토큰으로 판단합니다.
     *
     * @param token JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    fun validateToken(token: String): Boolean = try {
        getClaims(token)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * JWT 토큰에서 사용자의 이메일(Subject)을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 subject (일반적으로 사용자 이메일)
     */
    fun getUsername(token: String): String = getClaims(token).subject

    /**
     * JWT 토큰에서 토큰의 목적(purpose) 클레임을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 목적 정보 (없으면 null)
     */
    fun getPurpose(token: String): String? = getClaims(token)["purpose"] as? String
} 