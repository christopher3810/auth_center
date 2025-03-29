package com.auth.exception

sealed class UserException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class UserNotFoundException private constructor(
    override val message: String,
    override val cause: Throwable? = null
) : UserException(message, cause) {

    companion object {
        fun byId(userId: Long, cause: Throwable? = null): UserNotFoundException =
            UserNotFoundException("사용자를 찾을 수 없습니다: userId - $userId", cause)

        fun byEmail(email: String, cause: Throwable? = null): UserNotFoundException =
            UserNotFoundException("사용자를 찾을 수 없습니다: email - $email", cause)

        fun byUsername(username: String, cause: Throwable? = null): UserNotFoundException =
            UserNotFoundException("사용자를 찾을 수 없습니다: username - $username", cause)
    }
}
class UserAccountDeactivatedException(message: String, cause: Throwable? = null) :
    UserException("비활성화된 계정입니다: $message", cause)

class InvalidUserInput(message: String, cause: Throwable? = null) :
    UserException("잘못된 입력: $message", cause)

class AlreadyUserExistsException(message: String, cause: Throwable? = null) :
    UserException(message, cause) {

    companion object {
        fun byEmail(email: String, cause: Throwable? = null): AlreadyUserExistsException =
            AlreadyUserExistsException("이미 사용 중인 이메일입니다: $email", cause)

        fun byUsername(username: String, cause: Throwable? = null): AlreadyUserExistsException =
            AlreadyUserExistsException("이미 사용 중인 사용자명입니다: $username", cause)
    }
}

