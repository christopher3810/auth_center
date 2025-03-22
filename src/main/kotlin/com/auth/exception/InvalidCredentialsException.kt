package com.auth.exception

/**
 * 사용자 인증 정보(아이디/비밀번호 등)가 유효하지 않을 때 발생하는 예외
 */
class InvalidCredentialsException(message: String) : RuntimeException(message) 