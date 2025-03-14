package com.auth.domain.auth.service

import com.auth.domain.auth.model.TokenPurpose
import com.auth.infrastructure.security.token.TokenBuilder

/**
 * 토큰 생성을 담당하는 도메인 서비스 인터페이스
 */
interface TokenGenerator {

    //TODO : 필요에 따라 User 도메인이 input type이 되어야함.
    //바로생성 (standard or 전략 받도록 수정)
    fun generateAccessToken(subject: String): String

    fun generateRefreshToken(subject: String): String

    fun generateOneTimeToken(subject: String, purpose: TokenPurpose): String

    //커스텀생성
    fun generateAccessTokenBuilder(subject: String): TokenBuilder

    fun generateRefreshTokenBuilder(subject: String): TokenBuilder

    fun generateOneTimeTokenBuilder(subject: String, purpose: TokenPurpose): TokenBuilder

}