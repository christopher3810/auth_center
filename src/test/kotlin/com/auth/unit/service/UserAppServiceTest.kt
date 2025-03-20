package com.auth.unit.service

import com.auth.application.user.service.UserAppService
import com.auth.domain.user.model.User
import com.auth.domain.user.service.UserDomainService
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.Password
import com.auth.domain.user.value.UserStatus
import com.auth.exception.UserNotFound
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
import java.time.LocalDateTime

//한글 테스트 시도
class UserAppServiceTest : DescribeSpec({

    isolationMode = IsolationMode.InstancePerLeaf
    
    // 테스트 대상 및 의존성 정의
    val userDomainService = mockk<UserDomainService>()
    val sut = UserAppService(userDomainService)  // SUT (System Under Test)
    
    // 테스트 데이터 준비
    val testUser = User(
        id = 1L,
        username = "testuser",
        email = Email("test@example.com"),
        password = Password.of("Password123!"),
        name = "테스트 사용자",
        phoneNumber = "01012345678",
        roles = setOf("ROLE_USER"),
        status = UserStatus.ACTIVE,
        createdAt = LocalDateTime.now().minusDays(1),
        updatedAt = LocalDateTime.now().minusDays(1)
    )
    
    describe("사용자 애플리케이션 서비스는") {
        
        context("신규 사용자 등록 시") {
            val email = "newuser@example.com"
            val username = "newuser"
            val password = "Password123!"
            val name = "신규 사용자"
            val phoneNumber = "01012345678"
            
            val newUser = User(
                id = 2L,
                username = username,
                email = Email(email),
                password = Password.of(password),
                name = name,
                phoneNumber = phoneNumber,
                roles = setOf("ROLE_USER"),
                status = UserStatus.INACTIVE
            )
            
            every { 
                userDomainService.createUser(username, email, password, name, phoneNumber) 
            } returns newUser
            
            it("도메인 서비스에 사용자 생성을 위임하고 생성된 사용자를 반환해야 한다") {
                val result = sut.registerUser(username, email, password, name, phoneNumber)
                
                result shouldNotBe null
                result.id shouldBe 2L
                result.username shouldBe username
                result.email.value shouldBe email
                result.status shouldBe UserStatus.INACTIVE
                
                verify { userDomainService.createUser(username, email, password, name, phoneNumber) }
            }
        }
        
        context("ID로 사용자 조회 시") {
            val userId = 1L
            
            it("존재하는 사용자일 경우 사용자 정보를 반환해야 한다") {
                every { userDomainService.findUserById(userId) } returns testUser
                
                val result = sut.getUserById(userId)
                
                result shouldNotBe null
                result?.id shouldBe userId
                
                verify { userDomainService.findUserById(userId) }
            }
            
            it("존재하지 않는 사용자일 경우 null을 반환해야 한다") {
                val nonExistentId = 999999999L
                every { userDomainService.findUserById(nonExistentId) } throws NoSuchElementException("사용자를 찾을 수 없습니다 : userId - $nonExistentId")

                shouldThrow<NoSuchElementException> {
                    sut.getUserById(nonExistentId)
                }
                
                verify { userDomainService.findUserById(nonExistentId) }
            }
        }
        
        context("이메일로 사용자 조회 시") {
            val email = "test@example.com"
            val emailObject = Email(email)
            
            it("존재하는 이메일일 경우 사용자 정보를 반환해야 한다") {
                every { userDomainService.findUserByEmail(emailObject) } returns testUser
                
                val result = sut.getUserByEmail(email)
                
                result shouldNotBe null
                result?.email?.value shouldBe email
                
                verify { userDomainService.findUserByEmail(emailObject) }
            }
            
            it("존재하지 않는 사용자 이메일일 경우 NoSuchElementException 을 발생시켜야 한다") {
                val nonExistentEmail = "nonexistent@example.com"
                val nonExistentEmailObject = Email(nonExistentEmail)
                
                every { userDomainService.findUserByEmail(nonExistentEmailObject) } throws NoSuchElementException("사용자를 찾을 수 없습니다 : email - $email")

                shouldThrow<NoSuchElementException> {
                    sut.getUserByEmail(nonExistentEmail)
                }

                verify { userDomainService.findUserByEmail(nonExistentEmailObject) }
            }
        }
        
        context("사용자 프로필 업데이트 시") {
            val userId = 1L
            val updatedName = "변경된 이름"
            val updatedPhoneNumber = "01098765432"
            
            it("존재하는 사용자일 경우 도메인 모델을 업데이트하고 저장해야 한다") {
                val userCopy = testUser.createCopy()
                userCopy.update(updatedName, updatedPhoneNumber)
                
                every { userDomainService.findUserById(userId) } returns testUser
                
                val capturedUser = slot<User>()
                every { userDomainService.saveUser(capture(capturedUser)) } returns userCopy
                
                val result = sut.updateUserProfile(userId, updatedName, updatedPhoneNumber)
                
                result shouldNotBe null
                result.name shouldBe updatedName
                result.phoneNumber shouldBe updatedPhoneNumber
                
                verify { userDomainService.findUserById(userId) }
                verify { userDomainService.saveUser(any()) }
            }
            
            it("존재하지 않는 사용자일 경우 예외를 발생시켜야 한다") {
                val nonExistentId = 9999999999L
                every { userDomainService.findUserById(nonExistentId) } throws NoSuchElementException("사용자를 찾을 수 없습니다 : userId - $nonExistentId")
                
                shouldThrow<UserNotFound> {
                    sut.updateUserProfile(nonExistentId, updatedName, updatedPhoneNumber)
                }
                
                verify { userDomainService.findUserById(nonExistentId) }
                verify(exactly = 0) { userDomainService.saveUser(any()) }
            }
        }
        
        context("비밀번호 변경 시") {
            val userId = 1L
            val currentPassword = "Password123!"
            val newPassword = "NewPassword123!"
            
            it("현재 비밀번호가 일치하면 비밀번호를 변경하고 성공을 반환해야 한다") {
                val userCopy = testUser.createCopy()
                userCopy.changePassword(Password.of(newPassword))
                
                every { userDomainService.findUserById(userId) } returns testUser
                every { userDomainService.saveUser(any()) } returns userCopy
                
                val result = sut.changeUserPassword(userId, currentPassword, newPassword)
                
                result shouldBe true
                
                verify { userDomainService.findUserById(userId) }
                verify { userDomainService.saveUser(any()) }
            }
            
            it("현재 비밀번호가 일치하지 않으면 변경 없이 실패를 반환해야 한다") {
                val wrongPassword = "WrongPassword123!"
                
                every { userDomainService.findUserById(userId) } returns testUser
                
                val result = sut.changeUserPassword(userId, wrongPassword, newPassword)
                
                result shouldBe false
                
                verify { userDomainService.findUserById(userId) }
                verify(exactly = 0) { userDomainService.saveUser(any()) }
            }
        }
        
        context("사용자 활성화 시") {
            it("비활성 상태인 사용자를 활성화하고 성공을 반환해야 한다") {
                val inactiveUser = testUser.createCopy().apply { status = UserStatus.INACTIVE }
                val userId = inactiveUser.id
                
                val activatedUser = inactiveUser.createCopy().apply { activate() }
                
                every { userDomainService.findUserById(userId) } returns inactiveUser
                every { userDomainService.saveUser(any()) } returns activatedUser
                
                val result = sut.activateUser(userId)
                
                result shouldBe true
                
                verify { userDomainService.findUserById(userId) }
                verify { userDomainService.saveUser(any()) }
            }
        }
        
        context("모든 사용자 조회 시") {
            it("사용자 목록을 반환해야 한다") {
                val userList = listOf(
                    testUser,
                    User(
                        id = 2L,
                        username = "anotheruser",
                        email = Email("another@example.com"),
                        password = Password.of("Password123!"),
                        name = "또 다른 사용자",
                        phoneNumber = "01012345678",
                        roles = setOf("ROLE_USER"),
                        status = UserStatus.ACTIVE
                    )
                )
                
                every { userDomainService.findAllUsers() } returns userList
                
                val result = sut.getAllUsers()
                
                result.size shouldBe 2
                result[0].id shouldBe 1L
                result[1].id shouldBe 2L
                
                verify { userDomainService.findAllUsers() }
            }
        }
    }
})

// 테스트용 복사 헬퍼 메서드
private fun User.createCopy(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email,
        password = this.password,
        name = this.name,
        phoneNumber = this.phoneNumber,
        roles = this.roles,
        status = this.status,
        lastLoginAt = this.lastLoginAt,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
} 