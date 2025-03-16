package com.auth.domain.user.value

class UserId(val value: Long) {

    init {
        require(value > 0) { "사용자 ID는 양수여야 합니다." }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserId
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        fun of(id: Long): UserId {
            return UserId(id)
        }
    }
} 