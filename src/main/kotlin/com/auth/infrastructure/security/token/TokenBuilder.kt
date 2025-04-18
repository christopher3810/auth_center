package com.auth.infrastructure.security.token
import java.util.Date
import javax.crypto.SecretKey

// TODO : tokenProvider 내부 롤 분리, TokenBuilder Interface를 거쳐서 사용하도록 수정.

/**
 * JWT 토큰 생성을 위한 빌더 인터페이스
 * DDD에서 도메인 서비스로서 토큰 생성 전략을 정의합니다.
 */
interface TokenBuilder {
    /**
     * 토큰에 추가 정보를 포함합니다.
     *
     * @param key 클레임 키
     * @param value 클레임 값
     * @return TokenBuilder 인스턴스
     */
    fun withClaim(
        key: String,
        value: Any,
    ): TokenBuilder

    /**
     * 토큰에 여러 추가 정보를 포함합니다.
     *
     * @param claims 추가할 클레임 맵
     * @return TokenBuilder 인스턴스
     */
    fun withClaims(claims: Map<String, Any>): TokenBuilder

    /**
     * 토큰 발행 시간을 설정합니다.
     *
     * @param issuedAt 발행 시간
     * @return TokenBuilder 인스턴스
     */
    fun withIssuedAt(issuedAt: Date): TokenBuilder

    /**
     * 설정된 정보로 토큰을 생성합니다.
     *
     * @return 생성된 JWT 토큰
     */
    fun build(): String

    companion object {
        /**
         * 기본 액세스 토큰 빌더를 생성합니다.
         * 짧은 만료 시간을 가진 일반적인 인증용 토큰입니다.
         *
         * @param subject 토큰의 주체(일반적으로 사용자 이메일)
         * @param expirationMs 토큰 만료 시간(밀리초)
         * @param key 서명에 사용할 키
         * @return TokenBuilder 인스턴스
         */
        fun accessTokenBuilder(
            subject: String,
            expirationMs: Long,
            key: SecretKey,
        ): TokenBuilder = AccessTokenBuilder(subject, expirationMs, key)

        /**
         * 리프레시 토큰 빌더를 생성합니다.
         * 긴 만료 시간을 가진 토큰으로, 액세스 토큰 갱신에 사용됩니다.
         *
         * @param subject 토큰의 주체(일반적으로 사용자 이메일)
         * @param expirationMs 토큰 만료 시간(밀리초)
         * @param key 서명에 사용할 키
         * @return TokenBuilder 인스턴스
         */
        fun refreshTokenBuilder(
            subject: String,
            expirationMs: Long,
            key: SecretKey,
        ): TokenBuilder = RefreshTokenBuilder(subject, expirationMs, key)

        /**
         * 권한 검증용 토큰 빌더를 생성합니다.
         * 사용자의 역할과 권한 정보를 포함하는 토큰입니다.
         *
         * @param subject 토큰의 주체(일반적으로 사용자 이메일)
         * @param expirationMs 토큰 만료 시간(밀리초)
         * @param key 서명에 사용할 키
         * @param roles 사용자 role 정보 미입력시 빈값으로 생성
         * @param permissions 사용자 권한 정보 미입력시 빈값으로 생성
         * @return TokenBuilder 인스턴스
         */
        fun authorizationTokenBuilder(
            subject: String,
            expirationMs: Long,
            key: SecretKey,
            roles: Set<String> = emptySet(),
            permissions: Set<String> = emptySet(),
        ): TokenBuilder = AuthorizationTokenBuilder(subject, expirationMs, key, roles, permissions)

        /**
         * 일회용 토큰 빌더를 생성합니다.
         * 이메일 인증, 비밀번호 재설정 등 일회성 작업에 사용되는 토큰입니다.
         *
         * @param subject 토큰의 주체(일반적으로 사용자 이메일)
         * @param expirationMs 토큰 만료 시간(밀리초)
         * @param key 서명에 사용할 키
         * @param purpose 토큰의 목적(예: TokenPurpose.EMAIL_VERIFICATION.value)
         * @return TokenBuilder 인스턴스
         */
        fun oneTimeTokenBuilder(
            subject: String,
            expirationMs: Long,
            key: SecretKey,
            purpose: String,
        ): TokenBuilder = OneTimeTokenBuilder(subject, expirationMs, key, purpose)
    }
}
