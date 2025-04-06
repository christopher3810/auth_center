package com.auth.api.rest.dto

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 페이징 파라미터를 위한 추상 클래스
 * API 컨트롤러에서 페이징을 구현할 때 상속하여 사용
 */
abstract class PaginationParameters {
    /**
     * 페이지 번호 (0부터 시작)
     */
    @Parameter(
        name = "page",
        description = "페이지 번호 (0부터 시작)",
        `in` = ParameterIn.QUERY,
        schema = Schema(type = "integer", defaultValue = "0", minimum = "0"),
        example = "0",
    )
    val page: Int = 0

    /**
     * 페이지 크기 (한 페이지에 표시할 항목 수)
     */
    @Parameter(
        name = "size",
        description = "페이지 크기 (한 페이지에 표시할 항목 수)",
        `in` = ParameterIn.QUERY,
        schema = Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "100"),
        example = "20",
    )
    val size: Int = 20

    /**
     * 정렬 기준 필드
     */
    @Parameter(
        name = "sort",
        description = "정렬 기준 필드 (예: createdAt,desc 또는 name,asc)",
        `in` = ParameterIn.QUERY,
        schema = Schema(type = "string"),
        example = "createdAt,desc",
    )
    val sort: String? = null
}

/**
 * 사용자 목록 조회 파라미터
 */
class UserListParameters : PaginationParameters() {
    /**
     * 사용자명 검색어
     */
    @Parameter(
        name = "username",
        description = "사용자명 검색어 (부분 일치)",
        `in` = ParameterIn.QUERY,
        schema = Schema(type = "string"),
        example = "john",
    )
    val username: String? = null

    /**
     * 이메일 검색어
     */
    @Parameter(
        name = "email",
        description = "이메일 검색어 (부분 일치)",
        `in` = ParameterIn.QUERY,
        schema = Schema(type = "string"),
        example = "example.com",
    )
    val email: String? = null

    /**
     * 사용자 상태 필터
     */
    @Parameter(
        name = "status",
        description = "사용자 상태 필터",
        `in` = ParameterIn.QUERY,
        schema = Schema(type = "string", allowableValues = ["ACTIVE", "INACTIVE", "LOCKED", "PENDING"]),
        example = "ACTIVE",
    )
    val status: String? = null
}
