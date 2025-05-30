package com.auth.unit.domain.auth.service

import com.auth.domain.auth.entity.RefreshTokenEntity
import com.auth.domain.auth.factory.RefreshTokenFactory
import com.auth.domain.auth.model.RefreshToken
import com.auth.domain.auth.repository.RefreshTokenRepository
import com.auth.domain.auth.service.RefreshTokenDomainService
import com.auth.domain.auth.service.TokenGenerator
import com.auth.infrastructure.audit.Traceable
import com.auth.infrastructure.config.JwtConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import java.time.LocalDateTime

class RefreshTokenServiceTest :
    DescribeSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        val repository = spyk<RefreshTokenRepository>()
        val tokenGenerator = mockk<TokenGenerator>(relaxed = true)
        val jwtConfig =
            JwtConfig(
                secret = "testSecretKeyForUnitTestingThatIsLongEnough1234567890",
                accessTokenValidityInSeconds = 3600,
                refreshTokenValidityInSeconds = 86400,
            )

        val sut = RefreshTokenDomainService(repository, tokenGenerator, jwtConfig)

        describe("RefreshTokenService는") {

            context("새 리프레시 토큰 생성 시") {
                mockkObject(RefreshTokenFactory.Companion)
                val userId = 1L
                val email = "test@example.com"
                val tokenString = "generated_refresh_token"
                every { tokenGenerator.generateRefreshTokenString(email, userId) } returns tokenString

                val now = LocalDateTime.now()
                val expiryDate = now.plusMinutes(jwtConfig.refreshTokenValidityInSeconds / 60)

                val fakeEntity =
                    RefreshTokenEntity(
                        id = 100L,
                        token = tokenString,
                        userId = userId,
                        userEmail = email,
                        expiryDate = expiryDate,
                        used = false,
                        revoked = false,
                        traceable = Traceable().apply { createdAt = now },
                    )
                val newRefreshTokenDomain =
                    RefreshToken(
                        id = 100L,
                        token = tokenString,
                        userId = userId,
                        userEmail = email,
                        expiryDate = expiryDate,
                        used = false,
                        revoked = false,
                    )
                every {
                    RefreshTokenFactory.createToken(
                        tokenString,
                        userId,
                        email,
                        jwtConfig.refreshTokenValidityInSeconds / 60,
                    )
                } returns newRefreshTokenDomain
                every { RefreshTokenFactory.createEntity(newRefreshTokenDomain) } returns fakeEntity
                every { repository.findById(newRefreshTokenDomain.id) } returns fakeEntity
                every { RefreshTokenFactory.createFromEntity(fakeEntity) } returns newRefreshTokenDomain
                every { repository.save(any()) } returns fakeEntity

                it("정상적인 사용자 정보가 주어지면 새 토큰을 생성하여 반환해야 한다") {

                    val result = sut.generateRefreshToken(email, userId)

                    result.token shouldBe tokenString
                    result.userId shouldBe userId
                    result.userEmail shouldBe email
                    result.used shouldBe false
                    result.revoked shouldBe false
                    result.id shouldBe 100L

                    verify { repository.save(match { it.token == tokenString }) }
                }
            }

            context("사용 가능한 리프레시 토큰이 주어졌을 때") {
                it("토큰 사용 처리 후 상태가 '사용됨'으로 변경되어야 한다") {

                    val tokenString = "valid_refresh_token"
                    val userId = 1L
                    val email = "test@example.com"
                    val now = LocalDateTime.now().plusMinutes(60)
                    val fakeEntity =
                        RefreshTokenEntity(
                            id = 101L,
                            token = tokenString,
                            userId = userId,
                            userEmail = email,
                            expiryDate = now,
                            used = false,
                            revoked = false,
                            traceable = Traceable().apply { createdAt = LocalDateTime.now() },
                        )

                    val updatedEntity =
                        RefreshTokenEntity(
                            id = fakeEntity.id,
                            token = fakeEntity.token,
                            userId = fakeEntity.userId,
                            userEmail = fakeEntity.userEmail,
                            expiryDate = fakeEntity.expiryDate,
                            used = true,
                            revoked = fakeEntity.revoked,
                            traceable = fakeEntity.traceable,
                        )

                    every { repository.findByToken(tokenString) } returns fakeEntity
                    every { repository.save(any()) } answers { updatedEntity }
                    every { repository.findById(any()) } returns fakeEntity
                    val resultOpt = sut.markTokenAsUsed(tokenString)

                    resultOpt?.used shouldBe true
                }

                it("토큰 차단 처리 후 상태가 '차단됨'으로 변경되어야 한다") {
                    val tokenString = "valid_refresh_token_for_revoke"
                    val userId = 2L
                    val email = "user2@example.com"
                    val now = LocalDateTime.now().plusMinutes(60)
                    val fakeEntity =
                        RefreshTokenEntity(
                            id = 102L,
                            token = tokenString,
                            userId = userId,
                            userEmail = email,
                            expiryDate = now,
                            used = false,
                            revoked = false,
                            traceable = Traceable().apply { createdAt = LocalDateTime.now() },
                        )

                    val revokedEntity =
                        RefreshTokenEntity(
                            id = fakeEntity.id,
                            token = fakeEntity.token,
                            userId = fakeEntity.userId,
                            userEmail = fakeEntity.userEmail,
                            expiryDate = fakeEntity.expiryDate,
                            used = fakeEntity.used,
                            revoked = true, // 이 부분만 변경
                            traceable = fakeEntity.traceable,
                        )

                    every { repository.findByToken(tokenString) } returns fakeEntity
                    every { repository.save(any()) } answers { revokedEntity }
                    every { repository.findById(any()) } returns fakeEntity
                    val resultOpt = sut.revokeToken(tokenString)

                    resultOpt?.revoked shouldBe true
                }
            }

            context("만료된 토큰 삭제 시") {
                it("만료된 토큰을 삭제하고 삭제된 건수를 반환해야 한다") {
                    every { repository.deleteAllExpiredTokens(any()) } returns 3
                    val deletedCount = sut.removeExpiredTokens()

                    deletedCount shouldBe 3
                }
            }

            context("토큰 생성 시 예외 상황") {
                it("유효하지 않은 토큰 문자열로 조회 시 null 을 반환해야 한다.") {
                    val invalidToken = "non_existing_token"
                    every { repository.findByToken(invalidToken) } returns null

                    val findByToken = sut.findByToken(invalidToken)
                    findByToken shouldBe null
                }
            }
        }
    })
