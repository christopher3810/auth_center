package com.auth.domain.user.factory

import com.auth.domain.user.model.User
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.Password
import com.auth.domain.user.value.UserStatus
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class UserFactoryTest : DescribeSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    describe("UserFactory는") {
        val username = "testuser"
        val email = "testuser@example.com"
        val rawPassword = "password123"
        val name = "Test User"
        val phone = "010-1234-5678"

        context("일반 사용자 등록 시") {
            it("일반 사용자는 주어진 정보로 생성될 때 INACTIVE 상태여야 한다") {
                val user: User = UserFactory.createUser(username, email, rawPassword, name, phone)
                user.username shouldBe username
                user.email shouldBe Email(email)
                user.password shouldNotBe null
                user.name shouldBe name
                user.phoneNumber shouldBe phone
                user.status shouldBe UserStatus.INACTIVE
            }
        }

        context("관리자에 의한 사용자 등록 시") {
            it("관리자에 의해 생성된 사용자는 ACTIVE 상태여야 한다") {
                val user: User = UserFactory.createUserByAdmin(username, email, rawPassword, name, phone)
                user.username shouldBe username
                user.email shouldBe Email(email)
                user.password shouldNotBe null
                user.name shouldBe name
                user.phoneNumber shouldBe phone
                user.status shouldBe UserStatus.ACTIVE
            }
        }
    }

})
