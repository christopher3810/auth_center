package com.auth.unit.app.service

import com.auth.application.auth.service.TokenBlacklistService
import com.auth.domain.auth.model.RefreshToken
import com.auth.domain.auth.service.RefreshTokenDomainService
import com.auth.domain.auth.service.TokenValidator
import io.jsonwebtoken.Claims
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class TokenBlacklistServiceTest :
    DescribeSpec({
        isolationMode = IsolationMode.InstancePerTest

        val tokenValidator = mockk<TokenValidator>(relaxed = true)
        val refreshTokenDomainService = mockk<RefreshTokenDomainService>(relaxed = true)

        lateinit var sut: TokenBlacklistService

        beforeTest {
            clearAllMocks() // 각 테스트마다 모킹 초기화
            sut = TokenBlacklistService(tokenValidator, refreshTokenDomainService)
        }

        describe("토큰을 블랙리스트에 추가할 때.") {
            context("토큰이 유효하지 않은 경우") {
                it("토큰 유효성 검사 실패로 경고 로그를 남기고 false 를 반환해야 한다") {
                    every { tokenValidator.validateToken("invalidToken") } returns false

                    val result = sut.addToBlacklist("invalidToken")

                    result shouldBe false
                    verify(exactly = 1) { tokenValidator.validateToken("invalidToken") }
                }
            }

            context("토큰은 유효하지만 사용자 ID 클레임이 없는 경우") {
                it("사용자 ID 없음에 대해 경고 로그를 남기고 false 를 반환해야 한다") {
                    // Create mocked Claims object
                    val claims = mockk<Claims>()
                    every { claims["type"] } returns "access"
                    every { claims["userId"] } returns null

                    every { tokenValidator.validateToken("tokenWithoutUserId") } returns true
                    every { tokenValidator.getClaims("tokenWithoutUserId") } returns claims

                    val result = sut.addToBlacklist("tokenWithoutUserId")

                    result shouldBe false
                }
            }

            context("토큰이 유효하며 refresh 토큰이고 성공적으로 폐기될 경우") {
                it("리프레시 토큰을 블랙리스트에 추가하고 true 를 반환해야 한다") {
                    val claims = mockk<Claims>()
                    val refreshTokenDomain = mockk<RefreshToken>()
                    every { claims["type"] } returns "refresh"
                    every { claims["userId"] } returns "123"

                    every { tokenValidator.validateToken("validRefreshToken") } returns true
                    every { tokenValidator.getClaims("validRefreshToken") } returns claims

                    // revokeToken 이 non-null 값을 반환하면 폐기가 성공한 것으로 간주
                    every { refreshTokenDomainService.revokeToken("validRefreshToken") } returns refreshTokenDomain

                    val result = sut.addToBlacklist("validRefreshToken")

                    result shouldBe true
                    verify { refreshTokenDomainService.revokeToken("validRefreshToken") }
                }
            }

            context("토큰이 유효하며 액세스 토큰인 경우") {
                it("해당 사용자의 모든 리프레시 토큰을 폐기하고 true 를 반환해야 한다") {
                    val claims = mockk<Claims>()
                    every { claims["type"] } returns "access"
                    every { claims["userId"] } returns "456"

                    every { tokenValidator.validateToken("validAccessToken") } returns true
                    every { tokenValidator.getClaims("validAccessToken") } returns claims
                    every { refreshTokenDomainService.revokeAllUserTokens(456) } returns 2

                    val result = sut.addToBlacklist("validAccessToken")

                    result shouldBe true
                    verify { refreshTokenDomainService.revokeAllUserTokens(456) }
                }
            }
        }

        describe("토큰이 블랙리스트인지 확인할 때") {
            context("토큰 타입이 refresh 이고, 토큰 레코드가 유효하지 않을 경우") {
                it("블랙리스트에 포함되어 있으므로 true 를 반환해야 한다") {
                    // Arrange
                    val claims = mockk<Claims>()
                    every { claims["type"] } returns "refresh"

                    every { tokenValidator.getClaims("refreshToken") } returns claims
                    val refreshTokenDomain = mockk<RefreshToken>()
                    every { refreshTokenDomainService.findByToken("refreshToken") } returns refreshTokenDomain
                    every { refreshTokenDomain.isValid() } returns false

                    // Act
                    val result = sut.isBlacklisted("refreshToken")

                    // Assert
                    result shouldBe true
                }
            }

            context("토큰 타입이 refresh 이나, 토큰 레코드가 유효한 경우") {
                it("블랙리스트에 포함되지 않았으므로 false 를 반환해야 한다") {
                    // Arrange
                    val claims = mockk<Claims>()
                    every { claims["type"] } returns "refresh"

                    every { tokenValidator.getClaims("refreshTokenValid") } returns claims
                    val tokenRecord = mockk<RefreshToken>()
                    every { refreshTokenDomainService.findByToken("refreshTokenValid") } returns tokenRecord
                    every { tokenRecord.isValid() } returns true

                    // Act
                    val result = sut.isBlacklisted("refreshTokenValid")

                    // Assert
                    result shouldBe false
                }
            }

            context("토큰 타입이 refresh 가 아닌 경우") {
                it("블랙리스트 관리는 refresh 토큰에만 적용되므로 false 를 반환해야 한다") {
                    // Arrange
                    val claims = mockk<Claims>()
                    every { claims["type"] } returns "access"

                    every { tokenValidator.getClaims("accessToken") } returns claims

                    // Act
                    val result = sut.isBlacklisted("accessToken")

                    // Assert
                    result shouldBe false
                }
            }
        }

        describe("만료된 토큰을 전부 제거 할떄") {
            context("서비스 내부에 토큰 제거 로직이 호출될 때") {
                it("만료된 토큰을 삭제하는 로직이 실행되어야 한다") {
                    // Arrange
                    every { refreshTokenDomainService.removeExpiredTokens() } returns 3

                    // Act
                    sut.cleanupExpiredTokens()

                    // Assert
                    verify { refreshTokenDomainService.removeExpiredTokens() }
                }
            }
        }
    })
