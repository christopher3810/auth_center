package com.auth.unit.domain.auth.factory

import com.auth.domain.auth.factory.AccessTokenFactory
import com.auth.domain.auth.model.AccessToken
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.temporal.ChronoUnit

class AccessTokenFactoryTest : DescribeSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    describe("AccessTokenFactory 는") {
        val sut = AccessTokenFactory()
        val tokenValue = "sample.access.token"
        val userId = 1L
        val subject = "user@example.com"
        val validityInSeconds = 3600L
        val roles = setOf("ROLE_USER", "ROLE_ADMIN")
        val permissions = setOf("READ", "WRITE")

        context("정상적인 입력 값이 주어졌을 때") {
            it("올바른 AccessToken 도메인 모델을 생성해야 한다") {
                val accessToken: AccessToken = sut.createAccessToken(
                    tokenValue = tokenValue,
                    userId = userId,
                    subject = subject,
                    validityInSeconds = validityInSeconds,
                    roles = roles,
                    permissions = permissions
                )
                accessToken.tokenValue shouldBe tokenValue
                accessToken.userId shouldBe userId
                accessToken.subject shouldBe subject
                accessToken.roles shouldBe roles
                accessToken.permissions shouldBe permissions

                val durationSeconds = ChronoUnit.SECONDS.between(accessToken.issuedAt, accessToken.expiresAt)
                durationSeconds shouldBe validityInSeconds
            }
        }
    }

})
