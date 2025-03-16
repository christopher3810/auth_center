package com.auth.domain.auth.factory

import com.auth.domain.auth.entity.RefreshTokenEntity
import com.auth.domain.auth.model.RefreshToken
import com.auth.infrastructure.audit.Traceable
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.Duration.between
import java.time.LocalDateTime

class RefreshTokenFactoryTest : DescribeSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    describe("RefreshTokenFactory는") {
        val subject = "user@example.com"
        val userId = 2L
        val expiryMinutes = 20160L  // 14일
        val now = LocalDateTime.now()
        val sut = RefreshTokenFactory

        context("새 리프레시 토큰을 생성할 때") {
            it("주어진 입력 값으로 RefreshToken 도메인 모델을 생성해야 한다") {
                val refreshToken: RefreshToken = sut.createToken(
                    token = "sample.refresh.token",
                    userId = userId,
                    userEmail = subject,
                    expiryTimeInMinutes = expiryMinutes
                )
                refreshToken.token shouldBe "sample.refresh.token"
                refreshToken.userId shouldBe userId
                refreshToken.userEmail shouldBe subject
                refreshToken.used shouldBe false
                refreshToken.revoked shouldBe false

                // 만료 시간이 현재 시간 + expiryMinutes로 설정되었는지 (시간 차이가 1분 이하)
                val diffMinutes = between(refreshToken.expiryDate, LocalDateTime.now().plusMinutes(expiryMinutes)).abs().toMinutes()
                diffMinutes shouldBe 0L

                // 도메인 이벤트가 등록되었는지 확인 (등록 후 consume하면 리스트가 1개여야 함)
                val events = refreshToken.consumeEvents()
                events shouldHaveSize 1
            }
        }

        context("리프레시 토큰 엔티티에서 도메인 모델을 생성할 때") {
            it("엔티티의 필드가 도메인 모델에 올바르게 매핑되어야 한다") {
                val entity = RefreshTokenEntity(
                    id = 10L,
                    token = "entity.token",
                    userId = userId,
                    userEmail = subject,
                    expiryDate = now.plusMinutes(expiryMinutes),
                    used = false,
                    revoked = false,
                    traceable = Traceable().apply { createdAt = now }
                )
                val refreshToken = sut.createFromEntity(entity)
                refreshToken.id shouldBe 10L
                refreshToken.token shouldBe "entity.token"
                refreshToken.userId shouldBe userId
                refreshToken.userEmail shouldBe subject
                refreshToken.used shouldBe false
                refreshToken.revoked shouldBe false
            }
        }
    }

})
