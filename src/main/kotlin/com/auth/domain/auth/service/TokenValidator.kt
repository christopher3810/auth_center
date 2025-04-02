package com.auth.domain.auth.service
import com.auth.domain.auth.model.TokenPurpose
import com.auth.domain.auth.model.TokenType
import io.jsonwebtoken.Claims
import java.time.Instant

/**
 * 토큰 검증을 담당하는 도메인 서비스 인터페이스
 *
 * 이 인터페이스는 JWT 토큰의 유효성을 검증하고
 * 토큰에서 필요한 정보를 추출하는 메서드를 정의.
 */
interface TokenValidator {
    /**
     * 토큰에서 JWT Claims 객체를 추출합니다.
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
    fun validateToken(token: String): Boolean

    /**
     * 토큰에서 사용자 식별자(Subject)를 추출합니다.
     * 일반적으로 사용자의 이메일이나 다른 고유 식별자가 될 수 있습니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 subject (일반적으로 사용자 이메일)
     */
    fun getSubject(token: String): String

    /**
     * 토큰에서 사용자 ID를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 사용자 ID (없으면 null)
     */
    fun getUserId(token: String): Long?

    /**
     * 토큰에서 목적(purpose) 정보를 추출합니다.
     * 일회용 토큰의 경우 이메일 인증, 비밀번호 재설정 등의 목적을 가질 수 있습니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 목적 정보 (없으면 null)
     */
    fun getPurpose(token: String): TokenPurpose?

    /**
     * 토큰에서 토큰 타입 정보를 추출합니다.
     * ACCESS, REFRESH, ONE_TIME 등의 타입이 있을 수 있습니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 타입 정보 (없으면 null)
     */
    fun getTokenType(token: String): TokenType?

    /**
     * 토큰에서 사용자 역할 정보를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰의 역할 목록 (없으면 빈 집합)
     */
    fun getRoles(token: String): Set<String>

    /**
     * 토큰에서 사용자 권한 정보를 추출합니다.
     */
    fun getPermissions(token: String): Set<String>

    /**
     * 토큰에서 만료 시간 정보를 추출합니다.
     * 보통 Instant 형태로 반환하여 이후 만료 여부나 남은 유효 시간을 계산할 수 있습니다.
     */
    fun getExpirationTime(token: String): Instant?
}
