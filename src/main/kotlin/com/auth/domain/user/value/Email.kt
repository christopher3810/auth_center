package com.auth.domain.user.value

import com.vito.common.util.patternValidator.PatternValidator
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class Email(
    @Column(name = "email", nullable = false, unique = true)
    val value: String,
) {
    init {
        require(isValid(value)) { "유효하지 않은 이메일 형식입니다: $value" }
    }

    companion object {
        fun isValid(email: String): Boolean = PatternValidator.isValidEmail(email)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Email
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value
}
