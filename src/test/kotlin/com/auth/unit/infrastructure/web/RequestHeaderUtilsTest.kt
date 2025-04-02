package com.auth.unit.infrastructure.web

import com.auth.infrastructure.web.RequestHeaderUtils
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class RequestHeaderUtilsTest :
    DescribeSpec({
        isolationMode = IsolationMode.InstancePerLeaf

        describe("RequestHeaderUtils.getCurrentUserInfo") {
            context("HTTP 요청이 없는 경우") {
                it("null을 반환해야 한다") {
                    RequestContextHolder.resetRequestAttributes()
                    RequestHeaderUtils.getCurrentUserInfo().shouldBeNull()
                }
            }

            context("관리자 포털 헤더와 이메일이 제공된 경우") {
                it("admin:이메일 형식으로 반환되어야 한다") {
                    val mockRequest = mockk<HttpServletRequest>()
                    every { mockRequest.getHeader(RequestHeaderUtils.REQUEST_SOURCE_HEADER) } returns RequestHeaderUtils.ADMIN_PORTAL_SOURCE
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_EMAIL_HEADER) } returns "admin@example.com"
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_ID_HEADER) } returns null
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_ROLE_HEADER) } returns null
                    val attributes = ServletRequestAttributes(mockRequest)
                    RequestContextHolder.setRequestAttributes(attributes)

                    RequestHeaderUtils.getCurrentUserInfo() shouldBe "admin:admin@example.com"

                    RequestContextHolder.resetRequestAttributes()
                }
            }

            context("일반 사용자 앱 헤더와 이메일이 제공된 경우") {
                it("user:이메일 형식으로 반환되어야 한다") {
                    val mockRequest = mockk<HttpServletRequest>()
                    every { mockRequest.getHeader(RequestHeaderUtils.REQUEST_SOURCE_HEADER) } returns RequestHeaderUtils.USER_APP_SOURCE
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_EMAIL_HEADER) } returns "user@example.com"
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_ID_HEADER) } returns null
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_ROLE_HEADER) } returns null
                    val attributes = ServletRequestAttributes(mockRequest)
                    RequestContextHolder.setRequestAttributes(attributes)

                    RequestHeaderUtils.getCurrentUserInfo() shouldBe "user:user@example.com"

                    RequestContextHolder.resetRequestAttributes()
                }
            }

            context("이메일이 없고 사용자 ID만 제공된 경우") {
                it("user:ID 형식으로 반환되어야 한다") {
                    val mockRequest = mockk<HttpServletRequest>()
                    every { mockRequest.getHeader(RequestHeaderUtils.REQUEST_SOURCE_HEADER) } returns null
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_EMAIL_HEADER) } returns ""
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_ID_HEADER) } returns "12345"
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_ROLE_HEADER) } returns null
                    val attributes = ServletRequestAttributes(mockRequest)
                    RequestContextHolder.setRequestAttributes(attributes)

                    RequestHeaderUtils.getCurrentUserInfo() shouldBe "user:12345"

                    RequestContextHolder.resetRequestAttributes()
                }
            }

            context("관리자 역할 헤더와 이메일이 제공된 경우") {
                it("admin:이메일 형식으로 반환되어야 한다") {
                    val mockRequest = mockk<HttpServletRequest>()
                    every { mockRequest.getHeader(RequestHeaderUtils.REQUEST_SOURCE_HEADER) } returns null
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_EMAIL_HEADER) } returns "adminrole@example.com"
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_ID_HEADER) } returns null
                    every { mockRequest.getHeader(RequestHeaderUtils.USER_ROLE_HEADER) } returns RequestHeaderUtils.ROLE_ADMIN
                    val attributes = ServletRequestAttributes(mockRequest)
                    RequestContextHolder.setRequestAttributes(attributes)

                    RequestHeaderUtils.getCurrentUserInfo() shouldBe "admin:adminrole@example.com"

                    RequestContextHolder.resetRequestAttributes()
                }
            }
        }
    })
