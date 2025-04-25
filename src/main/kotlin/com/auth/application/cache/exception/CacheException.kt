package com.auth.application.cache.exception

/**
 * 캐시 작업 관련 모든 예외의 기본 클래스
 * sealed class를 사용하여 예외 계층 구조를 정의합니다.
 */
sealed class CacheException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /**
     * 캐시 연결 예외
     * 캐시 서버 연결 관련 문제 발생 시 사용됩니다.
     */
    class ConnectionException(
        override val message: String = "캐시 서버 연결 실패",
        override val cause: Throwable? = null,
    ) : CacheException(message, cause)

    /**
     * 캐시 키 존재하지 않음 예외
     * 요청한 캐시 키가 없을 때 사용됩니다.
     */
    class KeyNotFoundException(
        val key: String,
        override val message: String = "캐시 키를 찾을 수 없음: $key",
        override val cause: Throwable? = null,
    ) : CacheException(message, cause)

    /**
     * 직렬화/역직렬화 예외
     * 객체 변환 중 오류 발생 시 사용됩니다.
     */
    class SerializationException(
        override val message: String = "객체 직렬화/역직렬화 실패",
        override val cause: Throwable? = null,
    ) : CacheException(message, cause)

    /**
     * 작업 타임아웃 예외
     * 캐시 작업이 시간 초과될 때 사용됩니다.
     */
    class OperationTimeoutException(
        override val message: String = "캐시 작업 시간 초과",
        override val cause: Throwable? = null,
    ) : CacheException(message, cause)

    /**
     * 토큰 관련 예외
     * 토큰 처리 중 발생하는 문제에 사용됩니다.
     */
    sealed class TokenException(
        override val message: String,
        override val cause: Throwable? = null,
    ) : CacheException(message, cause) {
        /**
         * 토큰이 블랙리스트에 있을 때 발생
         */
        class BlacklistedException(
            val tokenId: String,
            override val message: String = "토큰이 블랙리스트에 있음: $tokenId",
            override val cause: Throwable? = null,
        ) : TokenException(message, cause)

        /**
         * 토큰 회전(갱신) 실패 시 발생
         */
        class RotationFailedException(
            val oldTokenId: String,
            val newTokenId: String,
            override val message: String = "토큰 회전 실패: $oldTokenId → $newTokenId",
            override val cause: Throwable? = null,
        ) : TokenException(message, cause)
    }

    /**
     * 일반 캐시 작업 예외
     * 기타 캐시 작업 중 발생한 문제에 사용됩니다.
     */
    class OperationException(
        override val message: String = "캐시 작업 실패",
        override val cause: Throwable? = null,
    ) : CacheException(message, cause)

    /**
     * 사용자 친화적 메시지를 반환합니다.
     */
    fun getUserFriendlyMessage(): String =
        when (this) {
            is ConnectionException, is OperationTimeoutException ->
                "일시적인 시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            is KeyNotFoundException ->
                "요청한 데이터를 찾을 수 없습니다."
            is SerializationException ->
                "데이터 형식 오류가 발생했습니다."
            is TokenException.BlacklistedException ->
                "이미 만료된 토큰입니다. 다시 로그인해주세요."
            is TokenException.RotationFailedException ->
                "토큰 갱신 중 오류가 발생했습니다. 다시 로그인해주세요."
            is OperationException ->
                "요청을 처리하는 중 오류가 발생했습니다."
        }
} 
