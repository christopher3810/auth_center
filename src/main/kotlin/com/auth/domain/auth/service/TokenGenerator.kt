package com.auth.domain.auth.service

import com.auth.domain.auth.model.TokenPurpose
import com.auth.infrastructure.security.token.TokenBuilder

/**
 * 토큰 문자열 생성을 담당하는 인터페이스
 * 
 * 이 인터페이스는 도메인 계층에 위치하지만, 도메인 모델에 직접 의존하지 않습니다.
 * 대신 토큰 생성에 필요한 최소한의 정보(subject, userId, roles 등)만 받아 
 * 토큰 문자열을 생성하는 책임을 가집니다.
 */
interface TokenGenerator {

    /**
     * 사용자 정보로부터 액세스 토큰 문자열 생성
     */
    fun generateAccessTokenString(subject: String, userId: Long,
                                  roles: Set<String> = emptySet(),
                                  permissions: Set<String> = emptySet()): String

    /**
     * 사용자 정보로부터 리프레시 토큰 문자열 생성
     */
    fun generateRefreshTokenString(subject: String, userId: Long): String

    /**
     * 사용자 정보로부터 일회용 토큰 문자열 생성
     */
    fun generateOneTimeTokenString(subject: String, userId: Long, purpose: TokenPurpose): String
}