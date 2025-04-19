package com.auth.application.cache

/**
 * 문자열 데이터를 캐시에 저장하고 검색하는 작업을 위한 인터페이스
 */
interface StringCacheAdaptor : CacheAdaptor {
    /**
     * 문자열 값을 저장합니다.
     * @param key 캐시 키
     * @param value 저장할 문자열 값
     */
    fun setString(
        key: String,
        value: String,
    )

    /**
     * 저장된 문자열 값을 조회합니다.
     * @param key 캐시 키
     * @return 저장된 문자열 값 또는 null (키가 존재하지 않을 경우)
     */
    fun getString(key: String): String?

    /**
     * 값을 증가시킵니다.
     * @param key 캐시 키
     * @param delta 증가시킬 값
     * @return 증가 후 결과 값
     */
    fun increment(
        key: String,
        delta: Long,
    ): Long
} 
