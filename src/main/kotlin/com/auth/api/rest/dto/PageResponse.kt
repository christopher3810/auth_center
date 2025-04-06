package com.auth.api.rest.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 페이징 응답 래퍼 DTO
 * 페이징된 결과를 표준화된 형식으로 제공
 */
@Schema(
    description = "페이징된 응답 객체",
    title = "PageResponse",
)
data class PageResponse<T>(
    @Schema(
        description = "현재 페이지 번호 (0부터 시작)",
        example = "0",
    )
    val page: Int,
    @Schema(
        description = "페이지 크기",
        example = "20",
    )
    val size: Int,
    @Schema(
        description = "전체 항목 수",
        example = "157",
    )
    val totalElements: Long,
    @Schema(
        description = "전체 페이지 수",
        example = "8",
    )
    val totalPages: Int,
    @Schema(
        description = "정렬 정보",
        example = "createdAt,desc",
    )
    val sort: String?,
    @Schema(
        description = "현재 페이지가 첫 페이지인지 여부",
        example = "true",
    )
    val first: Boolean,
    @Schema(
        description = "현재 페이지가 마지막 페이지인지 여부",
        example = "false",
    )
    val last: Boolean,
    @Schema(
        description = "실제 데이터 목록",
    )
    val content: List<T>,
) {
    /**
     * 페이징 메타데이터를 담는 내부 클래스
     */
    @Schema(
        description = "페이징 메타데이터",
        title = "PageMetadata",
    )
    data class PageMetadata(
        @Schema(
            description = "현재 페이지 번호 (0부터 시작)",
            example = "0",
        )
        val page: Int,
        @Schema(
            description = "페이지 크기",
            example = "20",
        )
        val size: Int,
        @Schema(
            description = "전체 항목 수",
            example = "157",
        )
        val totalElements: Long,
        @Schema(
            description = "전체 페이지 수",
            example = "8",
        )
        val totalPages: Int,
    )

    /**
     * 현재 페이지의 메타데이터만 추출
     */
    @Schema(hidden = true)
    fun getMetadata(): PageMetadata =
        PageMetadata(
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )

    companion object {
        /**
         * Spring Data의 Page 객체로부터 PageResponse 생성
         */
        fun <T, R> from(
            page: org.springframework.data.domain.Page<T>,
            content: List<R>,
        ): PageResponse<R> =
            PageResponse(
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                sort = page.sort.toString(),
                first = page.isFirst,
                last = page.isLast,
                content = content,
            )
    }
}
