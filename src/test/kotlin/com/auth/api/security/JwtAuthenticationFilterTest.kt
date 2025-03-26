package com.auth.api.security

import com.auth.application.auth.dto.UserTokenInfo
import com.auth.application.auth.service.TokenAppService
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import jakarta.servlet.DispatcherType
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class JwtAuthenticationFilterTest : DescribeSpec({
    
    isolationMode = IsolationMode.InstancePerLeaf

    lateinit var sut: JwtAuthenticationFilter

    val tokenAppService = mockk<TokenAppService>(relaxed = false)
    val request = mockk<HttpServletRequest>()
    val response = mockk<HttpServletResponse>()
    val filterChain = mockk<FilterChain>(relaxUnitFun = true)

    beforeTest {

        SecurityUtils.clearSecurityContext()

        sut = JwtAuthenticationFilter(tokenAppService)

        every { request.getAttribute(any()) } returns null
        every { request.removeAttribute(any()) } just runs
        every { request.getDispatcherType() } returns DispatcherType.REQUEST
        every { request.setAttribute(any(), any()) } just runs
    }

    describe("JWT 인증 필터는") {
        context("유효한 JWT 토큰이 주어졌을 때") {
            val validToken = "valid.jwt.token"
            val userInfo = UserTokenInfo(
                id = 1L,
                email = "user@example.com",
                roles = setOf("USER", "ADMIN"),
                additionalClaims = emptyMap()
            )

            beforeTest {
                every { request.getHeader("Authorization") } returns "Bearer $validToken"
                every { tokenAppService.validateToken(validToken) } returns true
                every { tokenAppService.getUserInfoFromToken(validToken) } returns userInfo

                sut.doFilter(request, response, filterChain)
            }

            it("토큰을 검증해야 한다") {
                verify(exactly = 1) { tokenAppService.validateToken(validToken) }
            }

            it("토큰에서 사용자 정보를 추출해야 한다") {
                verify(exactly = 1) { tokenAppService.getUserInfoFromToken(validToken) }
            }

            it("SecurityContext 에 올바른 인증 정보를 설정해야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                authentication.shouldNotBe(null)
                authentication.shouldBeInstanceOf<UsernamePasswordAuthenticationToken>()
                authentication.principal.shouldBe(userInfo)
                authentication.isAuthenticated.shouldBe(true)
            }

            it("필터 체인을 계속 진행해야 한다") {
                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }

        context("인증 헤더가 없을 때") {
            beforeTest {
                every { request.getHeader("Authorization") } returns null

                sut.doFilter(request, response, filterChain)
            }

            it("토큰 검증을 시도하지 않아야 한다") {
                verify(exactly = 0) { tokenAppService.validateToken(any()) }
            }

            it("인증 정보를 설정하지 않아야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                authentication.shouldBe(null)
            }

            it("필터 체인을 계속 진행해야 한다") {
                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }

        context("Bearer 형식이 아닌 인증 헤더가 주어졌을 때") {
            beforeTest {
                every { request.getHeader("Authorization") } returns "Basic dXNlcjpwYXNzd29yZA=="

                sut.doFilter(request, response, filterChain)
            }

            it("토큰 검증을 시도하지 않아야 한다") {
                verify(exactly = 0) { tokenAppService.validateToken(any()) }
            }

            it("인증 정보를 설정하지 않아야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                authentication.shouldBe(null)
            }

            it("필터 체인을 계속 진행해야 한다") {
                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }

        context("유효하지 않은 JWT 토큰이 주어졌을 때") {
            val invalidToken = "invalid.jwt.token"

            beforeTest {
                every { request.getHeader("Authorization") } returns "Bearer $invalidToken"
                every { tokenAppService.validateToken(invalidToken) } returns false

                sut.doFilter(request, response, filterChain)
            }

            it("토큰 검증을 수행해야 한다") {
                verify(exactly = 1) { tokenAppService.validateToken(invalidToken) }
            }

            it("사용자 정보를 추출하지 않아야 한다") {
                verify(exactly = 0) { tokenAppService.getUserInfoFromToken(any()) }
            }

            it("인증 정보를 설정하지 않아야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                authentication.shouldBe(null)
            }

            it("필터 체인을 계속 진행해야 한다") {
                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }
        }

        context("토큰 처리 중 예외가 발생했을 때") {
            val token = "exception.jwt.token"

            beforeTest {
                every { request.getHeader("Authorization") } returns "Bearer $token"
                every { tokenAppService.validateToken(token) } throws RuntimeException("토큰 검증 실패")

                sut.doFilter(request, response, filterChain)
            }

            it("예외를 흡수하고 필터 체인을 계속 진행해야 한다") {
                verify(exactly = 1) { filterChain.doFilter(request, response) }
            }

            it("인증 정보를 설정하지 않아야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                authentication.shouldBe(null)
            }
        }
    }
})
