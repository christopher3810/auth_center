package com.auth.domain.auth.token

import com.auth.domain.auth.model.TokenPurpose
import com.auth.domain.user.model.User

/**
 * 토큰 생성을 담당하는 도메인 서비스 인터페이스
 */
interface TokenGenerator {
    /**
     * 사용자 정보를 기반으로 토큰을 생성합니다.
     */
    //fun generateAccessToken(user: User): String

    //fun generateRefreshToken(user: User): String

    //fun generateOneTimeToken(user: User, purpose: TokenPurpose): String

    /**
     * 이메일을 기반으로 토큰 생성합니다.
     */
    fun generateAccessToken(email: String): String

    fun generateRefreshToken(email: String): String

    fun generateOneTimeToken(email: String, purpose: TokenPurpose): String

} 