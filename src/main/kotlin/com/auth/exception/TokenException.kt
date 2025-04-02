package com.auth.exception

/**
 * 토큰 관련 예외
 */
sealed class TokenException(
    message: String,
) : RuntimeException(message)

/**
 * 토큰이 유효하지 않은 경우 발생하는 예외
 */
class InvalidTokenException(
    message: String,
) : TokenException(message)

/**
 * 토큰이 만료된 경우 발생하는 예외
 */
class TokenExpiredException(
    message: String,
) : TokenException(message)

/**
 * 토큰에서 필요한 정보를 추출할 수 없는 경우 발생하는 예외
 */
class TokenExtractionException(
    message: String,
) : TokenException(message)
