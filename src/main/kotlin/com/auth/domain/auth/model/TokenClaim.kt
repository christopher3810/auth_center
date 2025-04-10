package com.auth.domain.auth.model

/**
 * 토큰 클레임 키를 정의하는 enum 클래스
 */
enum class TokenClaim(
    val value: String,
) {
    TYPE("type"),
    USER_ID("userId"),
    ROLES("roles"),
    PERMISSIONS("permissions"),
    PURPOSE("purpose"),
    USED("used"),
    ;

    companion object {
        fun fromValue(value: String): TokenClaim? = entries.find { claim: TokenClaim -> claim.value == value }
    }
}
