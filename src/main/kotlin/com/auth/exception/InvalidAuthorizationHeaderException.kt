package com.auth.exception

/**
 * Authorization 헤더가 누락되었거나 형식이 올바르지 않을 때 발생하는 예외
 */
class InvalidAuthorizationHeaderException(
    message: String,
) : RuntimeException(message)
