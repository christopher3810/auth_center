package com.auth.domain.user.value

import com.vito.common.util.patternValidator.PatternValidator
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Embeddable
class Password private constructor(
    @Column(name = "password", nullable = false)
    val hashedValue: String,
) {
    /**
     * 비밀번호 검증
     */
    fun matches(rawPassword: String): Boolean = PASSWORD_ENCODER.matches(rawPassword, hashedValue)

    companion object {
        private val PASSWORD_ENCODER: PasswordEncoder = BCryptPasswordEncoder()

        /**
         * 평문 비밀번호로부터 생성
         */
        fun of(rawPassword: String): Password {
            // 비밀번호 정책 검증
            require(PatternValidator.isValidPassword(rawPassword)) { "비밀번호는 최소 8자 이상이어야 하며, 문자와 숫자를 포함해야 합니다." }
            return Password(PASSWORD_ENCODER.encode(rawPassword))
        }

        /**
         * 해시된 비밀번호로부터 생성 (마이그레이션 등 특수 경우용)
         */
        fun fromHashed(hashedPassword: String): Password = Password(hashedPassword)
    }

    override fun toString(): String = "Password(hashedValue=********)"
}
