package com.auth.unit.service

import com.auth.application.auth.dto.TokenDto
import com.auth.application.auth.dto.UserTokenInfo
import com.auth.application.auth.service.TokenAppService
import com.auth.domain.auth.factory.AccessTokenFactory
import com.auth.domain.auth.model.AccessToken
import com.auth.domain.auth.model.RefreshToken
import com.auth.domain.auth.model.TokenPurpose
import com.auth.domain.auth.service.RefreshTokenDomainService
import com.auth.domain.auth.service.TokenGenerator
import com.auth.domain.auth.service.TokenValidator
import com.auth.domain.user.model.User
import com.auth.domain.user.service.UserDomainService
import com.auth.domain.user.value.UserStatus
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExtractionException
import com.auth.infrastructure.config.JwtConfig
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import java.time.Instant
import java.time.LocalDateTime
import java.util.Optional

class TokenServiceTest : DescribeSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testData = object {
        val userId = 123L
        val email = "test@example.com"
        val roles = setOf("ROLE_USER", "ROLE_ADMIN")
        val permissions = setOf("READ", "WRITE")
        val additionalClaims = mapOf("customKey" to "customValue")
        val sampleAccessToken = "sample.access.token"
        val sampleRefreshToken = "sample.refresh.token"
        val sampleOneTimeToken = "sample.onetime.token"
        val tokenType = "Bearer"

        // 기본 JWT 설정
        val jwtConfig = JwtConfig(
            secret = "testSecretKeyForJwtTokenServiceTestLongEnough",
            accessTokenValidityInSeconds = 3600L,
            refreshTokenValidityInSeconds = 86400L
        )

        val userInfo = UserTokenInfo(
            id = userId,
            email = email,
            roles = roles,
            additionalClaims = additionalClaims
        )

        val refreshTokenEntity = RefreshToken(
            id = 1L,
            token = sampleRefreshToken,
            userId = userId,
            userEmail = email,
            expiryDate = LocalDateTime.now().plusDays(14),
            used = false,
            revoked = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val accessTokenModel = AccessToken(
            tokenValue = sampleAccessToken,
            userId = userId,
            subject = email,
            issuedAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(3600),
            roles = roles,
            permissions = permissions
        )

        // 기본 클레임 생성 헬퍼 메서드
        fun createDefaultClaims(includeUserId: Boolean = true, includeRoles: Boolean = true): Claims {
            val claimsBuilder = Jwts.claims()
                .subject(email)
                .add("customKey", "customValue")

            if (includeUserId) {
                claimsBuilder.add("userId", userId.toString())
            }
            if (includeRoles) {
                claimsBuilder.add("roles", "ROLE_USER,ROLE_ADMIN")
            }

            return claimsBuilder.build()
        }
    }

    describe("토큰 서비스는") {
        context("사용자 인증 정보가 주어졌을 때") {
            // 필요한 모의 객체 생성
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockRefreshTokenDomainService = mockk<RefreshTokenDomainService>()
            val mockAccessTokenFactory = mockk<AccessTokenFactory>()
            val mockUserDomainService = mockk<UserDomainService>()

            // 토큰 생성 메서드 모킹
            every {
                mockTokenGenerator.generateAccessTokenString(
                    testData.email,
                    testData.userId,
                    testData.roles,
                    emptySet()
                )
            } returns testData.sampleAccessToken

            every {
                mockRefreshTokenDomainService.generateRefreshToken(
                    testData.email,
                    testData.userId
                )
            } returns testData.refreshTokenEntity

            val sut = TokenAppService(
                mockTokenGenerator,
                mockTokenValidator,
                mockRefreshTokenDomainService,
                mockAccessTokenFactory,
                mockUserDomainService,
                testData.jwtConfig
            )

            val tokenDto by lazy { sut.generateTokens(testData.userInfo) }

            it("유효한 액세스 토큰과 리프레시 토큰을 생성해야 한다") {
                tokenDto.accessToken shouldBe testData.sampleAccessToken
                tokenDto.refreshToken shouldBe testData.refreshTokenEntity.token
                tokenDto.tokenType shouldBe TokenDto.TOKEN_TYPE_BEARER
                tokenDto.expiresIn shouldBe testData.jwtConfig.accessTokenValidityInSeconds
                //tokenDto.refreshTokenExpiresIn shouldBe 20159L // 14 x 24 x 60 오차범위 1 20160L 될수도 있음. TODO : 오차범위 1 해결
                tokenDto.isNewRefreshToken shouldBe true

                verifySequence {
                    mockTokenGenerator.generateAccessTokenString(
                        testData.email,
                        testData.userId,
                        testData.roles,
                        emptySet()
                    )
                    mockRefreshTokenDomainService.generateRefreshToken(
                        testData.email,
                        testData.userId
                    )
                }
            }

            context("사용자 역할이 없는 경우에도") {
                val userInfoWithoutRoles = UserTokenInfo(
                    id = testData.userId,
                    email = testData.email,
                    roles = emptySet(),
                    additionalClaims = testData.additionalClaims
                )

                every {
                    mockTokenGenerator.generateAccessTokenString(
                        testData.email,
                        testData.userId,
                        emptySet(),
                        emptySet()
                    )
                } returns testData.sampleAccessToken

                it("정상적으로 토큰을 생성해야 한다") {
                    val result = sut.generateTokens(userInfoWithoutRoles)
                    result.accessToken shouldBe testData.sampleAccessToken
                    result.refreshToken shouldBe testData.refreshTokenEntity.token

                    verify {
                        mockTokenGenerator.generateAccessTokenString(
                            testData.email,
                            testData.userId,
                            emptySet(),
                            emptySet()
                        )
                    }
                }
            }
        }

        context("유효한 리프레시 토큰으로 토큰을 갱신할 때") {
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockRefreshTokenDomainService = mockk<RefreshTokenDomainService>()
            val mockAccessTokenFactory = mockk<AccessTokenFactory>()
            val mockUserDomainService = mockk<UserDomainService>()
            val mockUser = mockk<User>()

            every { mockTokenValidator.validateToken(testData.sampleRefreshToken) } returns true
            every { mockTokenValidator.getSubject(testData.sampleRefreshToken) } returns testData.email
            every { mockTokenValidator.getUserId(testData.sampleRefreshToken) } returns testData.userId
            every { mockTokenValidator.getRoles(testData.sampleRefreshToken) } returns testData.roles

            every {
                mockTokenGenerator.generateAccessTokenString(
                    testData.email,
                    testData.userId,
                    testData.roles,
                    emptySet()
                )
            } returns testData.sampleAccessToken

            every { mockRefreshTokenDomainService.findByToken(testData.sampleRefreshToken) } returns
                    testData.refreshTokenEntity

            every { mockRefreshTokenDomainService.markTokenAsUsed(testData.sampleRefreshToken) } returns
                    testData.refreshTokenEntity

            every {
                mockUserDomainService.findUserById(testData.userId)
            } returns mockUser

            val sut = TokenAppService(
                mockTokenGenerator,
                mockTokenValidator,
                mockRefreshTokenDomainService,
                mockAccessTokenFactory,
                mockUserDomainService,
                testData.jwtConfig
            )

            context("새 리프레시 토큰을 요청했을 때") {

                val newRefreshToken = RefreshToken(
                    id = 2L,
                    token = "new.refresh.token",
                    userId = testData.userId,
                    userEmail = testData.email,
                    expiryDate = LocalDateTime.now().plusDays(14),
                    used = false,
                    revoked = false,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                every {
                    mockRefreshTokenDomainService.generateRefreshToken(testData.email, testData.userId)
                } returns newRefreshToken
                every { mockUser.isActive() } returns true
                every { mockUser.status } returns UserStatus.ACTIVE
                every { mockUser.roles } returns testData.roles

                it("새 액세스 토큰과 새 리프레시 토큰을 모두 생성해야 한다") {

                    val result = sut.refreshAccessToken(testData.sampleRefreshToken)

                    result.accessToken shouldBe testData.sampleAccessToken
                    result.refreshToken shouldBe newRefreshToken.token
                    result.isNewRefreshToken shouldBe true
                    result.refreshTokenIssuedAt shouldNotBe null

                    verify {
                        mockRefreshTokenDomainService.generateRefreshToken(testData.email, testData.userId)
                    }
                }
            }
        }

        context("유효하지 않은 리프레시 토큰으로 토큰을 갱신할 때") {
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockRefreshTokenDomainService = mockk<RefreshTokenDomainService>()
            val mockAccessTokenFactory = mockk<AccessTokenFactory>()
            val mockUserDomainService = mockk<UserDomainService>()

            val sut = TokenAppService(
                mockTokenGenerator,
                mockTokenValidator,
                mockRefreshTokenDomainService,
                mockAccessTokenFactory,
                mockUserDomainService,
                testData.jwtConfig
            )

            context("토큰 유효성 검증에 실패한 경우") {
                val invalidToken = "invalid.refresh.token"
                every { mockTokenValidator.validateToken(invalidToken) } returns false

                it("InvalidTokenException 예외를 발생시켜야 한다") {
                    shouldThrow<InvalidTokenException> {
                        sut.refreshAccessToken(invalidToken)
                    }

                    verify { mockTokenValidator.validateToken(invalidToken) }
                }
            }

            context("토큰이 데이터베이스에 존재하지 않는 경우") {
                val unknownToken = "unknown.refresh.token"

                every { mockTokenValidator.validateToken(unknownToken) } returns true
                every { mockRefreshTokenDomainService.findByToken(unknownToken) } returns null

                it("InvalidTokenException 예외를 발생시켜야 한다") {
                    shouldThrow<InvalidTokenException> {
                        sut.refreshAccessToken(unknownToken)
                    }
                }
            }

            context("토큰이 만료되었거나 이미 사용되었거나 취소된 경우") {
                val invalidRefreshToken = "used.refresh.token"
                val invalidModel = RefreshToken(
                    id = testData.refreshTokenEntity.id,
                    token = testData.refreshTokenEntity.token,
                    userId = testData.refreshTokenEntity.userId,
                    userEmail = testData.refreshTokenEntity.userEmail,
                    expiryDate = testData.refreshTokenEntity.expiryDate,
                    used = true,  // 이미 사용된 토큰으로 설정
                    revoked = testData.refreshTokenEntity.revoked,
                    createdAt = testData.refreshTokenEntity.createdAt,
                    updatedAt = testData.refreshTokenEntity.updatedAt
                )

                every { mockTokenValidator.validateToken(invalidRefreshToken) } returns true
                every { mockRefreshTokenDomainService.findByToken(invalidRefreshToken) } returns
                        invalidModel

                it("InvalidTokenException 예외를 발생시켜야 한다") {
                    shouldThrow<InvalidTokenException> {
                        sut.refreshAccessToken(invalidRefreshToken)
                    }
                }
            }
        }

        context("토큰을 검증할 때") {
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockRefreshTokenDomainService = mockk<RefreshTokenDomainService>()
            val mockAccessTokenFactory = mockk<AccessTokenFactory>()
            val mockUserDomainService = mockk<UserDomainService>()

            val sut = TokenAppService(
                mockTokenGenerator,
                mockTokenValidator,
                mockRefreshTokenDomainService,
                mockAccessTokenFactory,
                mockUserDomainService,
                testData.jwtConfig
            )

            context("유효한 토큰이 주어진 경우") {
                every { mockTokenValidator.validateToken("valid.token") } returns true

                it("true를 반환해야 한다") {
                    val result = sut.validateToken("valid.token")
                    result shouldBe true

                    verify { mockTokenValidator.validateToken("valid.token") }
                }
            }

            context("유효하지 않은 토큰이 주어진 경우") {
                every { mockTokenValidator.validateToken("invalid.token") } returns false

                it("false를 반환해야 한다") {
                    val result = sut.validateToken("invalid.token")
                    result shouldBe false

                    verify { mockTokenValidator.validateToken("invalid.token") }
                }
            }
        }

        context("리프레시 토큰을 무효화할 때") {
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockRefreshTokenDomainService = mockk<RefreshTokenDomainService>()
            val mockAccessTokenFactory = mockk<AccessTokenFactory>()
            val mockUserDomainService = mockk<UserDomainService>()

            val sut = TokenAppService(
                mockTokenGenerator,
                mockTokenValidator,
                mockRefreshTokenDomainService,
                mockAccessTokenFactory,
                mockUserDomainService,
                testData.jwtConfig
            )

            context("존재하는 토큰을 무효화하는 경우") {
                every { mockRefreshTokenDomainService.revokeToken(testData.sampleRefreshToken) } returns
                        testData.refreshTokenEntity

                it("성공적으로 무효화하고 true를 반환해야 한다") {
                    val result = sut.revokeRefreshToken(testData.sampleRefreshToken)
                    result shouldBe true

                    verify { mockRefreshTokenDomainService.revokeToken(testData.sampleRefreshToken) }
                }
            }

            context("존재하지 않는 토큰을 무효화하는 경우") {
                val nonExistentToken = "non.existent.token"

                every { mockRefreshTokenDomainService.revokeToken(nonExistentToken) } returns null

                it("실패하고 false를 반환해야 한다") {
                    val result = sut.revokeRefreshToken(nonExistentToken)
                    result shouldBe false

                    verify { mockRefreshTokenDomainService.revokeToken(nonExistentToken) }
                }
            }
        }

        context("사용자의 모든 토큰을 무효화할 때") {
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockRefreshTokenDomainService = mockk<RefreshTokenDomainService>()
            val mockAccessTokenFactory = mockk<AccessTokenFactory>()
            val mockUserDomainService = mockk<UserDomainService>()

            val sut = TokenAppService(
                mockTokenGenerator,
                mockTokenValidator,
                mockRefreshTokenDomainService,
                mockAccessTokenFactory,
                mockUserDomainService,
                testData.jwtConfig
            )

            context("토큰이 있는 사용자의 경우") {
                every { mockRefreshTokenDomainService.revokeAllUserTokens(testData.userId) } returns 5

                it("무효화된 토큰 개수를 반환해야 한다") {
                    val result = sut.revokeAllUserTokens(testData.userId)
                    result shouldBe 5

                    verify { mockRefreshTokenDomainService.revokeAllUserTokens(testData.userId) }
                }
            }

            context("토큰이 없는 사용자의 경우") {
                val userWithoutTokens = 999L
                every { mockRefreshTokenDomainService.revokeAllUserTokens(userWithoutTokens) } returns 0

                it("0을 반환해야 한다") {
                    val result = sut.revokeAllUserTokens(userWithoutTokens)
                    result shouldBe 0

                    verify { mockRefreshTokenDomainService.revokeAllUserTokens(userWithoutTokens) }
                }
            }
        }

        context("토큰에서 사용자 정보를 추출할 때") {
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockRefreshTokenDomainService = mockk<RefreshTokenDomainService>()
            val mockAccessTokenFactory = mockk<AccessTokenFactory>()
            val mockUserDomainService = mockk<UserDomainService>()

            val token = "valid.token"
            every { mockTokenValidator.getClaims(token) } returns testData.createDefaultClaims()
            every { mockTokenValidator.getSubject(token) } returns testData.email
            every { mockTokenValidator.getUserId(token) } returns testData.userId
            every { mockTokenValidator.getRoles(token) } returns testData.roles


            val sut = TokenAppService(
                mockTokenGenerator,
                mockTokenValidator,
                mockRefreshTokenDomainService,
                mockAccessTokenFactory,
                mockUserDomainService,
                testData.jwtConfig
            )

            context("모든 필수 클레임이 포함된 경우") {
                it("올바른 사용자 정보를 반환해야 한다") {
                    val userInfo = sut.getUserInfoFromToken(token)

                    userInfo.email shouldBe testData.email
                    userInfo.id shouldBe testData.userId
                    userInfo.roles shouldBe testData.roles
                    userInfo.additionalClaims["customKey"] shouldBe "customValue"

                    verify {
                        mockTokenValidator.getClaims(token)
                        mockTokenValidator.getSubject(token)
                        mockTokenValidator.getUserId(token)
                        mockTokenValidator.getRoles(token)
                    }
                }
            }

            context("사용자 ID가 없는 경우") {
                val tokenWithoutUserId = "token.without.userId"

                every { mockTokenValidator.getClaims(tokenWithoutUserId) } returns
                        testData.createDefaultClaims(includeUserId = false)
                every { mockTokenValidator.getSubject(tokenWithoutUserId) } returns testData.email
                every { mockTokenValidator.getUserId(tokenWithoutUserId) } returns null

                it("TokenExtractionException 예외를 발생시켜야 한다") {
                    val exception = shouldThrow<TokenExtractionException> {
                        sut.getUserInfoFromToken(tokenWithoutUserId)
                    }

                    exception.message shouldBe "User ID not found in token"
                }
            }

            context("역할 정보가 없는 경우") {
                val tokenWithoutRoles = "token.without.roles"

                every { mockTokenValidator.getClaims(tokenWithoutRoles) } returns
                        testData.createDefaultClaims(includeRoles = false)
                every { mockTokenValidator.getSubject(tokenWithoutRoles) } returns testData.email
                every { mockTokenValidator.getUserId(tokenWithoutRoles) } returns testData.userId
                every { mockTokenValidator.getRoles(tokenWithoutRoles) } returns emptySet()

                it("빈 역할 세트와 함께 사용자 정보를 반환해야 한다") {
                    val userInfo = sut.getUserInfoFromToken(tokenWithoutRoles)
                    userInfo.roles shouldBe emptySet()
                }
            }
        }

        context("액세스 토큰 문자열로부터 도메인 모델을 생성할 때") {
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockRefreshTokenDomainService = mockk<RefreshTokenDomainService>()
            val mockAccessTokenFactory = mockk<AccessTokenFactory>()
            val mockUserDomainService = mockk<UserDomainService>()

            val sut = TokenAppService(
                mockTokenGenerator,
                mockTokenValidator,
                mockRefreshTokenDomainService,
                mockAccessTokenFactory,
                mockUserDomainService,
                testData.jwtConfig
            )

            context("유효한 토큰이 제공된 경우") {
                val validToken = "valid.access.token"
                val expirationTime = Instant.now().plusSeconds(3600)

                every { mockTokenValidator.validateToken(validToken) } returns true
                every { mockTokenValidator.getSubject(validToken) } returns testData.email
                every { mockTokenValidator.getUserId(validToken) } returns testData.userId
                every { mockTokenValidator.getRoles(validToken) } returns testData.roles
                every { mockTokenValidator.getPermissions(validToken) } returns testData.permissions
                every { mockTokenValidator.getExpirationTime(validToken) } returns expirationTime
                every {
                    mockAccessTokenFactory.createAccessToken(
                        tokenValue = validToken,
                        userId = testData.userId,
                        subject = testData.email,
                        validityInSeconds = any(),
                        roles = testData.roles,
                        permissions = testData.permissions
                    )
                } returns testData.accessTokenModel

                it("유효한 AccessToken 도메인 모델을 반환해야 한다") {
                    val result = sut.createAccessTokenModel(validToken)

                    result shouldBe testData.accessTokenModel
                }
            }

            context("유효하지 않은 토큰이 제공된 경우") {
                val invalidToken = "invalid.token"

                every { mockTokenValidator.validateToken(invalidToken) } returns false

                it("null 을 반환해야 한다") {
                    val result = sut.createAccessTokenModel(invalidToken)
                    result shouldBe null
                }
            }

            context("필수 클레임이 누락된 토큰이 제공된 경우") {
                val tokenWithoutUserId = "token.without.userId"

                every { mockTokenValidator.validateToken(tokenWithoutUserId) } returns true
                every { mockTokenValidator.getSubject(tokenWithoutUserId) } returns testData.email
                every { mockTokenValidator.getUserId(tokenWithoutUserId) } returns null

                it("null 을 반환해야 한다") {
                    val result = sut.createAccessTokenModel(tokenWithoutUserId)
                    result shouldBe null
                }
            }
        }

        context("일회용 토큰을 생성할 때") {
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockRefreshTokenDomainService = mockk<RefreshTokenDomainService>()
            val mockAccessTokenFactory = mockk<AccessTokenFactory>()
            val mockUserDomainService = mockk<UserDomainService>()

            val sut = TokenAppService(
                mockTokenGenerator,
                mockTokenValidator,
                mockRefreshTokenDomainService,
                mockAccessTokenFactory,
                mockUserDomainService,
                testData.jwtConfig
            )

            val purposes = listOf(
                TokenPurpose.EMAIL_VERIFICATION,
                TokenPurpose.PASSWORD_RESET,
                TokenPurpose.ACCOUNT_ACTIVATION
            )

            purposes.forAll { purpose ->
                every {
                    mockTokenGenerator.generateOneTimeTokenString(
                        testData.email,
                        testData.userId,
                        purpose
                    )
                } returns testData.sampleOneTimeToken

                it("${purpose.value} 목적을 가진 유효한 일회용 토큰을 생성해야 한다") {
                    val token = sut.generateOneTimeToken(testData.email, testData.userId, purpose)
                    token shouldBe testData.sampleOneTimeToken
                }
            }
        }

        context("만료된 토큰 정리를 요청할 때") {
            val mockTokenGenerator = mockk<TokenGenerator>()
            val mockTokenValidator = mockk<TokenValidator>()
            val mockRefreshTokenDomainService = mockk<RefreshTokenDomainService>()
            val mockAccessTokenFactory = mockk<AccessTokenFactory>()
            val mockUserDomainService = mockk<UserDomainService>()

            val sut = TokenAppService(
                mockTokenGenerator,
                mockTokenValidator,
                mockRefreshTokenDomainService,
                mockAccessTokenFactory,
                mockUserDomainService,
                testData.jwtConfig
            )

            context("만료된 토큰이 있는 경우") {
                every { mockRefreshTokenDomainService.removeExpiredTokens() } returns 10

                it("삭제된 토큰 수를 반환해야 한다") {
                    val result = sut.cleanupExpiredTokens()
                    result shouldBe 10
                }
            }

            context("만료된 토큰이 없는 경우") {
                every { mockRefreshTokenDomainService.removeExpiredTokens() } returns 0

                it("0을 반환해야 한다") {
                    val result = sut.cleanupExpiredTokens()
                    result shouldBe 0
                }
            }
        }
    }
})