package com.auth.application.cache

import java.time.Duration

/**
 * 캐시 관리의 기본 작업을 위한 인터페이스
 * 모든 캐시 매니저가 공통으로 가져야 할 기능들을 정의합니다.
 */
interface CacheAdaptor {
    /**
     * 키가 존재하는지 확인합니다.
     * @param key 캐시 키
     * @return 키 존재 여부
     */
    fun hasKey(key: String): Boolean

    /**
     * 키를 삭제합니다.
     * @param key 삭제할 캐시 키
     * @return 삭제 성공 여부
     */
    fun delete(key: String): Boolean

    /**
     * 패턴에 일치하는 모든 키를 삭제합니다.
     * @param pattern 키 패턴
     * @return 삭제된 키 수
     */
    fun deleteByPattern(pattern: String): Long

    /**
     * 키의 만료 시간을 설정합니다.
     * @param key 캐시 키
     * @param ttl 만료 시간
     * @return 설정 성공 여부
     */
    fun setExpire(key: String, ttl: Duration): Boolean
} 