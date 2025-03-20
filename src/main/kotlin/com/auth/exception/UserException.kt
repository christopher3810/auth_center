package com.auth.exception

sealed class UserException (message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class UserNotFound(userId: Long, cause: Throwable? = null) :
    UserException("사용자를 찾을 수 없습니다: userId - $userId", cause)

class InvalidUserInput(message: String, cause: Throwable? = null) :
    UserException("잘못된 입력: $message", cause)

class AlreadyUserExists(userId: Long, cause: Throwable? = null) :
    UserException("이미 존재하는 사용자입니다: userId - $userId", cause)

