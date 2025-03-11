package com.auth.unit.infrastructure.audit

import com.auth.infrastructure.audit.HeaderAuditorAware
import com.auth.infrastructure.web.RequestHeaderUtils
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class HeaderAuditorAwareTest : DescribeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    describe("HeaderAuditorAware.getCurrentAuditor") {
        context("HTTP 요청이 없는 경우") {
            it("Optional.empty를 반환해야 한다") {
                RequestContextHolder.resetRequestAttributes()
                val auditor = HeaderAuditorAware().getCurrentAuditor()
                auditor.isPresent shouldBe false
            }
        }

        context("유효한 HTTP 요청과 사용자 이메일 헤더가 제공된 경우") {
            it("Optional 에 user:이메일이 포함되어 반환되어야 한다") {
                val mockRequest = mockk<HttpServletRequest>()
                every { mockRequest.getHeader(RequestHeaderUtils.REQUEST_SOURCE_HEADER) } returns RequestHeaderUtils.USER_APP_SOURCE
                every { mockRequest.getHeader(RequestHeaderUtils.USER_EMAIL_HEADER) } returns "user@example.com"
                every { mockRequest.getHeader(RequestHeaderUtils.USER_ID_HEADER) } returns null
                every { mockRequest.getHeader(RequestHeaderUtils.USER_ROLE_HEADER) } returns null
                val attributes = ServletRequestAttributes(mockRequest)
                RequestContextHolder.setRequestAttributes(attributes)

                val auditor = HeaderAuditorAware().getCurrentAuditor()
                auditor.orElse(null) shouldBe "user:user@example.com"

                RequestContextHolder.resetRequestAttributes()
            }
        }
    }
})