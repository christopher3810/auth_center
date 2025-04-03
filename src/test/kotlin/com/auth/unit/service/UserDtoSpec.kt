package com.auth.unit.service

import com.auth.api.rest.dto.user.UserDetailResponse
import com.auth.api.rest.dto.user.UserProfileResponse
import com.auth.api.rest.dto.user.UserRegistrationResponse
import com.auth.api.rest.dto.user.UserSummaryResponse
import com.auth.domain.user.model.User
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.Password
import com.auth.domain.user.value.UserStatus
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class UserDtoSpec :
    DescribeSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        describe("사용자 DTO 변환은") {
            // 테스트 데이터 준비
            val nowDate = LocalDateTime.now()
            val beforeDate = nowDate.minusDays(1)

            val testUser =
                User(
                    id = 1L,
                    username = "testuser",
                    email = Email("test@example.com"),
                    password = Password.of("Password123!"),
                    name = "테스트 사용자",
                    phoneNumber = "01012345678",
                    roles = setOf("ROLE_USER"),
                    status = UserStatus.ACTIVE,
                    lastLoginAt = nowDate,
                    createdAt = beforeDate,
                    updatedAt = nowDate,
                )

            val secondTestUser =
                User(
                    id = 2L,
                    username = "testuser2",
                    email = Email("test2@example.com"),
                    password = Password.of("Password123!"),
                    name = "테스트 사용자2",
                    phoneNumber = "01087654321",
                    roles = setOf("ROLE_USER", "ROLE_ADMIN"),
                    status = UserStatus.INACTIVE,
                    lastLoginAt = null,
                    createdAt = beforeDate,
                    updatedAt = beforeDate,
                )

            context("상세 정보 응답 DTO 변환 시") {
                val sut = UserDetailResponse.from(testUser)

                it("모든 필드가 올바르게 변환되어야 한다") {
                    sut.id shouldBe 1L
                    sut.username shouldBe "testuser"
                    sut.email shouldBe "test@example.com"
                    sut.name shouldBe "테스트 사용자"
                    sut.phoneNumber shouldBe "01012345678"
                    sut.roles shouldContainExactly setOf("ROLE_USER")
                    sut.status shouldBe "ACTIVE"
                    sut.isActive shouldBe true
                    sut.lastLoginAt shouldBe nowDate
                    sut.createdAt shouldBe beforeDate
                    sut.updatedAt shouldBe nowDate
                }
            }

            context("요약 정보 응답 DTO 변환 시") {
                val sut = UserSummaryResponse.from(testUser)

                it("필요한 필드만 정확히 변환되어야 한다") {
                    sut.id shouldBe 1L
                    sut.username shouldBe "testuser"
                    sut.email shouldBe "test@example.com"
                    sut.name shouldBe "테스트 사용자"
                    sut.status shouldBe "ACTIVE"
                    sut.isActive shouldBe true
                    sut.lastLoginAt shouldBe nowDate
                }
            }

            context("사용자 목록을 요약 DTO 목록으로 변환 시") {
                val userList = listOf(testUser, secondTestUser)
                val sut = UserSummaryResponse.from(userList)

                it("목록의 모든 사용자가 올바르게 변환되어야 한다") {
                    sut.size shouldBe 2

                    // 첫 번째 사용자 검증
                    sut[0].id shouldBe 1L
                    sut[0].username shouldBe "testuser"
                    sut[0].isActive shouldBe true

                    // 두 번째 사용자 검증
                    sut[1].id shouldBe 2L
                    sut[1].username shouldBe "testuser2"
                    sut[1].status shouldBe "INACTIVE"
                    sut[1].isActive shouldBe false
                    sut[1].lastLoginAt shouldBe null
                }
            }

            context("프로필 정보 응답 DTO 변환 시") {
                val sut = UserProfileResponse.from(testUser)

                it("개인정보 관련 필드만 변환되어야 한다") {
                    sut.username shouldBe "testuser"
                    sut.email shouldBe "test@example.com"
                    sut.name shouldBe "테스트 사용자"
                    sut.phoneNumber shouldBe "01012345678"
                    sut.roles shouldContainExactly setOf("ROLE_USER")
                    sut.lastLoginAt shouldBe nowDate
                }
            }

            context("사용자 등록 결과 응답 DTO 변환 시") {
                val sut = UserRegistrationResponse.from(testUser)

                it("등록 관련 필드와 기본 메시지가 포함되어야 한다") {
                    sut.id shouldBe 1L
                    sut.username shouldBe "testuser"
                    sut.email shouldBe "test@example.com"
                    sut.message shouldBe "사용자 등록이 완료되었습니다. 이메일 인증을 통해 계정을 활성화해주세요."
                }
            }

            context("다양한 역할을 가진 사용자 변환 시") {
                it("역할 목록이 정확히 변환되어야 한다") {
                    val sut = UserDetailResponse.from(secondTestUser)
                    sut.roles shouldContainExactlyInAnyOrder setOf("ROLE_USER", "ROLE_ADMIN")
                }
            }

            context("상태가 다른 사용자 변환 시") {
                it("활성화 여부가 상태에 맞게 설정되어야 한다") {
                    val activeUserResponse = UserDetailResponse.from(testUser)
                    val deActiveUserResponse = UserDetailResponse.from(secondTestUser)

                    activeUserResponse.status shouldBe "ACTIVE"
                    activeUserResponse.isActive shouldBe true

                    deActiveUserResponse.status shouldBe "INACTIVE"
                    deActiveUserResponse.isActive shouldBe false
                }
            }
        }
    })
