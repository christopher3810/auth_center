package com.auth.application.cache

/**
 * 인증 토큰 관리를 위한 캐시 인터페이스
 * 토큰의 저장, 검증, 갱신 및 삭제 기능을 제공합니다.
 */
interface TokenCacheAdaptor : CacheAdaptor {
    /**
     * 사용자 ID에 해당하는 액세스 토큰 저장
     * @param userId 사용자 식별자
     * @param token 액세스 토큰
     * @param expirationSeconds 토큰 만료 시간(초)
     */
    fun saveAccessToken(
        userId: String,
        token: String,
        expirationSeconds: Long,
    )

    /**
     * 사용자 ID에 해당하는 리프레시 토큰 저장
     * @param userId 사용자 식별자
     * @param token 리프레시 토큰
     * @param expirationSeconds 토큰 만료 시간(초)
     */
    fun saveRefreshToken(
        userId: String,
        token: String,
        expirationSeconds: Long,
    )

    /**
     * 사용자 ID로 액세스 토큰 조회
     * @param userId 사용자 식별자
     * @return 액세스 토큰 (없을 경우 null)
     */
    fun getAccessToken(userId: String): String?

    /**
     * 사용자 ID로 리프레시 토큰 조회
     * @param userId 사용자 식별자
     * @return 리프레시 토큰 (없을 경우 null)
     */
    fun getRefreshToken(userId: String): String?

    /**
     * 토큰 유효성 검사
     * @param token 검사할 토큰
     * @return 토큰 유효 여부
     */
    fun validateToken(token: String): Boolean

    /**
     * 토큰 블랙리스트 등록
     * @param token 블랙리스트에 등록할 토큰
     * @param expirationSeconds 블랙리스트 유지 시간(초)
     */
    fun blacklistToken(
        token: String,
        expirationSeconds: Long,
    )

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token 확인할 토큰
     * @return 블랙리스트 포함 여부
     */
    fun isTokenBlacklisted(token: String): Boolean

    /**
     * 사용자의 모든 토큰 삭제 (로그아웃 시 사용)
     * @param userId 사용자 식별자
     */
    fun removeAllUserTokens(userId: String)

    /**
     * 특정 디바이스에 대한 사용자 토큰 삭제
     * @param userId 사용자 식별자
     * @param deviceId 디바이스 식별자
     */
    fun removeUserTokenForDevice(
        userId: String,
        deviceId: String,
    )
} 
