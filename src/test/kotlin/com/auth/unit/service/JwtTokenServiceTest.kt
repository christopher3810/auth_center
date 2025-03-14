package com.auth.unit.service

import com.auth.infrastructure.security.token.JwtTokenAdaptor
import com.auth.application.facade.JwtTokenService
import com.auth.domain.auth.model.TokenClaim
import com.auth.infrastructure.security.token.TokenBuilder
import com.auth.exception.TokenException
import com.auth.infrastructure.config.JwtConfig
import com.auth.application.auth.dto.UserTokenInfo
import com.auth.domain.auth.service.TokenGenerator
import com.auth.domain.auth.service.TokenValidator
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class JwtTokenServiceTest : DescribeSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testData = object {
        val userId = 123L
        val email = "test@example.com"
        val roles = setOf("ROLE_USER", "ROLE_ADMIN")
        val additionalClaims = mapOf("customKey" to "customValue")
        val sampleAccessToken = "sample.access.token"
        val sampleRefreshToken = "sample.refresh.token"

        // 기본 JWT 설정
        val jwtConfig = JwtConfig.builder()
            .secret("testSecretKeyForJwtTokenServiceTestLongEnough")
            .expirationMs(3600000L)
            .refreshExpirationMs(86400000L)
            .build()

        val userInfo = UserTokenInfo(
            id = userId,
            email = email,
            roles = roles,
            additionalClaims = additionalClaims
        )

        // 기본 클레임 생성 헬퍼 메서드
        fun createDefaultClaims(includeUserId: Boolean = true, includeRoles: Boolean = true): Claims {
            // 0.12.x에서 권장하는 방식: ClaimsBuilder 사용
            val claimsBuilder = Jwts.claims()
                .subject(email)
                .add("customKey", "customValue")

            if (includeUserId) {
                claimsBuilder.add(TokenClaim.USER_ID.value, userId.toString())
            }
            if (includeRoles) {
                claimsBuilder.add(TokenClaim.ROLES.value, "ROLE_USER,ROLE_ADMIN")
            }

            return claimsBuilder.build()
        }

        // 빌더 체이닝 메서드 모킹 헬퍼
        fun setupTokenBuilderMock(
            mockBuilder: TokenBuilder = mockk(),
            returnToken: String = sampleAccessToken
        ): TokenBuilder {
            every { mockBuilder.withClaim(any(), any()) } returns mockBuilder
            every { mockBuilder.build() } returns returnToken
            return mockBuilder
        }
    }

    describe("JWT 토큰 서비스는") {
        context("토큰 생성할 때") {

            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockAccessTokenBuilder = testData.setupTokenBuilderMock(mockk(), testData.sampleAccessToken)
            val mockRefreshTokenBuilder = testData.setupTokenBuilderMock(mockk(), testData.sampleRefreshToken)

            // 토큰 생성 메서드 모킹
            every { mockTokenGenerator.generateAccessTokenBuilder(testData.email) } returns mockAccessTokenBuilder
            every { mockTokenGenerator.generateRefreshTokenBuilder(testData.email) } returns mockRefreshTokenBuilder

            val sut = JwtTokenService(mockTokenGenerator,mockTokenValidator, testData.jwtConfig)

            val tokenDto by lazy { sut.generateTokens(testData.userInfo) }

            it("액세스 토큰과 리프레시 토큰을 모두 반환해야 한다") {
                tokenDto.accessToken shouldBe testData.sampleAccessToken
                tokenDto.refreshToken shouldBe testData.sampleRefreshToken
            }

            it("만료 시간을 초 단위로 변환하여 반환해야 한다") {
                tokenDto.expiresIn shouldBe testData.jwtConfig.expirationMs / 1000
            }

            it("사용자 ID와 역할 정보가 각 토큰에 포함되어야 한다") {
                tokenDto.accessToken shouldBe testData.sampleAccessToken
                tokenDto.refreshToken shouldBe testData.sampleRefreshToken

                verify {
                    mockTokenGenerator.generateAccessTokenBuilder(testData.email)
                    mockTokenGenerator.generateRefreshTokenBuilder(testData.email)
                }

                verify(atLeast = 1) { mockAccessTokenBuilder.withClaim(any(), any()) }
                verify(atLeast = 1) { mockRefreshTokenBuilder.withClaim(any(), any()) }
            }
        }

        context("토큰을 리프레시 시킬 때") {
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockAccessTokenBuilder = testData.setupTokenBuilderMock()

            every { mockTokenValidator.validateToken(testData.sampleRefreshToken) } returns true
            every { mockTokenValidator.getClaims(testData.sampleRefreshToken) } returns testData.createDefaultClaims()
            every { mockTokenGenerator.generateAccessTokenBuilder(testData.email) } returns mockAccessTokenBuilder

            val sut = JwtTokenService(mockTokenGenerator,mockTokenValidator, testData.jwtConfig)
            val refreshedTokenDto by lazy { sut.refreshToken(testData.sampleRefreshToken) }

            it("새 액세스 토큰을 생성하고 기존 리프레시 토큰을 유지해야 한다") {
                refreshedTokenDto.accessToken shouldBe testData.sampleAccessToken
                refreshedTokenDto.refreshToken shouldBe testData.sampleRefreshToken
                refreshedTokenDto.expiresIn shouldBe testData.jwtConfig.expirationMs / 1000

                verify {
                    mockTokenValidator.validateToken(testData.sampleRefreshToken)
                    mockTokenValidator.getClaims(testData.sampleRefreshToken)
                    mockTokenGenerator.generateAccessTokenBuilder(testData.email)
                }
            }
        }

        context("유효하지 않은 리프레시 토큰 처리할 때") {
            val invalidRefreshToken = "invalid.refresh.token"

            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator> {
                every { validateToken(invalidRefreshToken) } returns false
            }

            val sut = JwtTokenService(mockTokenGenerator, mockTokenValidator, testData.jwtConfig)

            it("TokenException이 발생해야 한다") {
                shouldThrow<TokenException> {
                    sut.refreshToken(invalidRefreshToken)
                }

                verify { mockTokenValidator.validateToken(invalidRefreshToken) }
                verify(exactly = 0) { mockTokenValidator.getClaims(any()) }
            }
        }

        context("토큰 검증할 때") {
            data class TokenTestCase(
                val token: String,
                val isValid: Boolean,
                val description: String
            )

            val testCases = listOf(
                TokenTestCase("valid.token", true, "유효한 토큰"),
                TokenTestCase("invalid.token", false, "유효하지 않은 토큰")
            )

            testCases.forAll { testCase ->

                val mockTokenGenerator = mockk<TokenGenerator>()
                val mockTokenValidator =  mockk<TokenValidator> {
                    every { validateToken(testCase.token) } returns testCase.isValid
                }


                val sut = JwtTokenService(mockTokenGenerator,mockTokenValidator, testData.jwtConfig)

                it("${testCase.description}은 ${testCase.isValid}를 반환해야 한다") {
                    sut.validateToken(testCase.token) shouldBe testCase.isValid
                    verify { mockTokenValidator.validateToken(testCase.token) }
                }
            }
        }

        context("토큰에서 사용자 정보 추출할 때") {
            val token = "test.token"
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator =  mockk<TokenValidator> {
                every { getClaims(token) } returns testData.createDefaultClaims()
            }

            val sut = JwtTokenService(mockTokenGenerator,mockTokenValidator, testData.jwtConfig)
            val extractedUserInfo by lazy { sut.getUserInfoFromToken(token) }

            it("이메일, 사용자 ID, 역할 정보, 추가 클레임이 올바르게 추출되어야 한다") {
                extractedUserInfo.email shouldBe testData.email
                extractedUserInfo.id shouldBe testData.userId
                extractedUserInfo.roles shouldBe setOf("ROLE_USER", "ROLE_ADMIN")
                extractedUserInfo.additionalClaims["customKey"] shouldBe "customValue"
            }
        }

        context("사용자 ID가 없는 토큰 처리할 때") {
            val tokenWithoutUserId = "token.without.userid"
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>(){
                every { getClaims(tokenWithoutUserId) } returns testData.createDefaultClaims(includeUserId = false)
            }

            val sut = JwtTokenService(mockTokenGenerator, mockTokenValidator, testData.jwtConfig)

            it("TokenException이 발생해야 한다") {
                val exception = shouldThrow<TokenException> {
                    sut.getUserInfoFromToken(tokenWithoutUserId)
                }

                exception.message shouldBe "User ID not found in token"
            }
        }

        context("역할정보가 없는 토큰 일 때") {
            val tokenWithoutRoles = "token.without.roles"
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockAccessTokenBuilder = testData.setupTokenBuilderMock()

            every { mockTokenValidator.getClaims(tokenWithoutRoles) } returns testData.createDefaultClaims(includeRoles = false)
            every { mockTokenValidator.validateToken(tokenWithoutRoles) } returns true
            every { mockTokenGenerator.generateAccessTokenBuilder(testData.email) } returns mockAccessTokenBuilder

            val sut = JwtTokenService(mockTokenGenerator, mockTokenValidator, testData.jwtConfig)

            it("empty 역할 세트를 반환하고, 리프레시시 ROLES 클레임이 추가되지 않아야 한다") {
                val userInfo = sut.getUserInfoFromToken(tokenWithoutRoles)
                userInfo.roles shouldBe emptySet()

                val refreshResult = sut.refreshToken(tokenWithoutRoles)

                refreshResult.accessToken shouldBe testData.sampleAccessToken

                verify {
                    mockTokenValidator.validateToken(tokenWithoutRoles)
                    mockTokenValidator.getClaims(tokenWithoutRoles)
                    mockTokenGenerator.generateAccessTokenBuilder(testData.email)
                }
            }
        }
    }
})