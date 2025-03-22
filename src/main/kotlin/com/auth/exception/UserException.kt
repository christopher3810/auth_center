package com.auth.exception

sealed class UserException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class UserNotFoundException private constructor(
    private val identifier: Identifier,
    override val message: String = generateMessage(identifier),
    override val cause: Throwable? = null
) : UserException(message, cause) {

    sealed interface Identifier {
        data class Id(val userId: Long) : Identifier
        data class Email(val email: String) : Identifier 
        data class Username(val username: String) : Identifier
    }

    companion object {
        fun byId(userId: Long, cause: Throwable? = null): UserNotFoundException =
            UserNotFoundException(Identifier.Id(userId), cause = cause)

        fun byEmail(email: String, cause: Throwable? = null): UserNotFoundException =
            UserNotFoundException(Identifier.Email(email), cause = cause)

        fun byUsername(username: String, cause: Throwable? = null): UserNotFoundException =
            UserNotFoundException(Identifier.Username(username), cause = cause)

        private fun generateMessage(identifier: Identifier): String = when(identifier) {
            is Identifier.Id -> "사용자를 찾을 수 없습니다: userId - ${identifier.userId}"
            is Identifier.Email -> "사용자를 찾을 수 없습니다: email - ${identifier.email}"
            is Identifier.Username -> "사용자를 찾을 수 없습니다: username - ${identifier.username}"
        }
    }
}

class InvalidUserInput(message: String, cause: Throwable? = null) :
    UserException("잘못된 입력: $message", cause)

class AlreadyUserExists(userId: Long, cause: Throwable? = null) :
    UserException("이미 존재하는 사용자입니다: userId - $userId", cause)

