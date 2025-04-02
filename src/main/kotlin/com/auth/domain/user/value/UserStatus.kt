package com.auth.domain.user.value

/**
 * 사용자 상태 열거형
 * 모델링 문서 참고: "status: 사용자 상태 (예: 활성/비활성, 잠금 등). 가입 후 이메일 검증 전이라면 일시적으로 Inactive 상태일 수 있고, 인증 실패 누적으로 Lock될 수 있습니다."
 */
enum class UserStatus(
    val description: String,
) {
    /**
     * 활성 상태 (이메일 인증 완료, 정상 사용 가능)
     */
    ACTIVE("활성화"),

    /**
     * 비활성 상태 (이메일 인증 미완료 또는 관리자에 의해 비활성화)
     */
    INACTIVE("비활성화"),

    /**
     * 잠금 상태 (인증 실패 누적이나 기타 보안 이슈로 인한 계정 잠금)
     */
    LOCKED("잠금"),

    /**
     * 휴면 상태 (장기간 미사용으로 인한 휴면 계정)
     */
    DORMANT("휴면"),

    /**
     * 삭제 예정 상태 (사용자 탈퇴 신청 후 데이터 보존 기간 중)
     */
    PENDING_DELETION("삭제 예정"),
    ;

    /**
     * 사용 가능 상태인지 확인 (로그인 가능 여부)
     */
    fun isUsable(): Boolean = this == ACTIVE

    /**
     * 계정 활성화 가능 상태인지 확인
     */
    fun canActivate(): Boolean = this == INACTIVE || this == DORMANT
}
