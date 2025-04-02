package com.auth.infrastructure.audit

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

/**
 * 엔티티의 감사(audit) 정보를 담는 임베디드 클래스
 * 엔티티 생성자, 수정자는 Http Header 기반으로 기입한다.
 * Admin 처리로 작성하는 경우에도 별도의 Admin 요청으로 Header 정보가 기입되어야 한다.
 * Batch 처리의 경우 추후 고민.
 */
@Embeddable
class Traceable {
    /**
     * 엔티티 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null

    /**
     * 엔티티 생성자
     */
    @CreatedBy
    @Column(name = "created_by", nullable = true, updatable = false, length = 100)
    var createdBy: String? = null

    /**
     * 엔티티 마지막 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = true)
    var updatedAt: LocalDateTime? = null

    /**
     * 엔티티 마지막 수정자
     */
    @LastModifiedBy
    @Column(name = "updated_by", nullable = true, length = 100)
    var updatedBy: String? = null
}
