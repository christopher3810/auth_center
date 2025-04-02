package com.auth.integration

import com.auth.annotation.JpaIntegrationTest
import com.auth.domain.user.entity.UserEntity
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.Password
import com.auth.infrastructure.audit.HeaderAuditorAware
import com.auth.infrastructure.config.JpaAuditingConfig
import com.auth.infrastructure.repository.UserJpaRepository
import com.auth.infrastructure.web.RequestHeaderUtils
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@JpaIntegrationTest
@Import(JpaAuditingConfig::class, HeaderAuditorAware::class)
class UserRepositoryIntegrationTest : DescribeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    lateinit var userJpaRepository: UserJpaRepository

    init {
        isolationMode = IsolationMode.InstancePerLeaf

        describe("JPA Auditing on User entity") {
            context("새로운 User 를 저장할 때") {
                it("createdAt, createdBy, updatedAt, updatedBy 필드가 자동으로 채워져야 한다") {
                    // 모의 HTTP 요청 설정
                    val mockRequest = mockk<HttpServletRequest>()
                    every { mockRequest.getHeader(RequestHeaderUtils.REQUEST_SOURCE_HEADER) } returns RequestHeaderUtils.USER_APP_SOURCE
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_EMAIL_HEADER) } returns "integration@example.com"
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_ID_HEADER) } returns null
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_ROLE_HEADER) } returns null
                    val attributes = ServletRequestAttributes(mockRequest)
                    RequestContextHolder.setRequestAttributes(attributes)

                    // 새로운 User 생성 및 저장
                    val user =
                        UserEntity(
                            userName = "integration_test",
                            email = Email("integration@example.com"),
                            password = Password.of("Password123!"),
                            name = "Integration Test",
                            phoneNumber = "010-1234-5678",
                        )
                    val savedUser = userJpaRepository.save(user)

                    // 감사 정보(Traceable)가 자동으로 채워졌는지 검증
                    savedUser.traceable.createdAt.shouldNotBeNull()
                    savedUser.traceable.createdBy.shouldNotBeNull()
                    savedUser.traceable.updatedAt.shouldNotBeNull()
                    savedUser.traceable.updatedBy.shouldNotBeNull()

                    savedUser.traceable.createdBy shouldContain "user:integration@example.com"
                    savedUser.traceable.updatedBy shouldContain "user:integration@example.com"

                    RequestContextHolder.resetRequestAttributes()
                }
            }
        }
    }
}
