package com.auth.integration.event

import com.auth.annotation.IntegrationTest
import com.auth.config.TestTransactionEventListener
import com.auth.domain.user.entity.UserEntity
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.Password
import com.auth.domain.user.value.UserStatus
import com.auth.infrastructure.web.RequestHeaderUtils
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import jakarta.persistence.EntityManager
import jakarta.servlet.http.HttpServletRequest
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

/**
 * TestTransactionEventListener 통합 테스트
 * DCI 스타일로 작성된 테스트로, 테스트 트랜잭션 이벤트 리스너의 동작을 검증합니다.
 */
@IntegrationTest
@TestExecutionListeners(
    listeners = [DependencyInjectionTestExecutionListener::class, TestTransactionEventListener::class],
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
class TestTransactionEventListenerTest(
    private val entityManager: EntityManager,
) : DescribeSpec({

    describe("TestTransactionEventListener는") {
        val sut = TestTransactionEventListener()
        beforeContainer {
            setupMockHttpRequest()
        }
        
        afterContainer {
            RequestContextHolder.resetRequestAttributes()
            clearAllMocks()
        }
        context("통합 테스트 환경에서") {
            val testUserIdentifier = "test-user-${UUID.randomUUID()}"
            
            beforeEach {
                // 각 테스트마다 새로운 HTTP 요청 헤더 설정
                setupMockHttpRequest()
            }
            
            it("데이터베이스에 저장된 엔티티는 정상적으로 조회되어야 한다") {
                // 테스트용 사용자 생성
                val userEntity = UserEntity(
                    userName = testUserIdentifier,
                    email = Email("$testUserIdentifier@example.com"),
                    password = Password.of("Test1234!"),
                    name = "Test User"
                )
                
                // 영속화
                entityManager.persist(userEntity)
                entityManager.flush()
                
                // 검증: 저장 직후에는 조회 가능해야 함
                val savedUser = entityManager.createQuery(
                    "SELECT u FROM UserEntity u WHERE u.userName = :userName", 
                    UserEntity::class.java
                )
                    .setParameter("userName", testUserIdentifier)
                    .singleResult
                
                savedUser shouldNotBe null
                savedUser.userName shouldBe testUserIdentifier
                
                // Auditing 정보가 올바르게 설정되었는지 확인
                savedUser.traceable.createdBy shouldNotBe null
                savedUser.traceable.createdBy shouldBe "user:test@example.com"
                savedUser.traceable.updatedBy shouldNotBe null
            }
            
            it("첫 번째 테스트 이후 롤백이 수행되면 테스트 데이터는 조회되지 않아야 한다") {
                // 이전 테스트에서 생성한 사용자를 조회 시도
                val result = entityManager.createQuery(
                    "SELECT COUNT(u) FROM UserEntity u WHERE u.userName = :userName",
                    Long::class.java
                )
                    .setParameter("userName", testUserIdentifier)
                    .singleResult
                
                // 롤백이 정상적으로 수행되었다면 해당 사용자는 존재하지 않아야 함
                result shouldBe 0L
            }
            
            it("여러 사용자를 생성한 후에도 롤백이 정상적으로 수행되어야 한다") {
                val batchPrefix = "batch-test-${UUID.randomUUID()}-"
                val userEntities = (1..5).map { i ->
                    UserEntity(
                        userName = "$batchPrefix$i",
                        email = Email("$batchPrefix$i@example.com"),
                        password = Password.of("Test1234!"),
                        name = "Batch Test User $i",
                        status = UserStatus.ACTIVE
                    )
                }
                
                // 다수의 사용자 저장
                userEntities.forEach { user ->
                    entityManager.persist(user)
                }
                entityManager.flush()
                
                // 저장 확인
                val savedCount = entityManager.createQuery(
                    "SELECT COUNT(u) FROM UserEntity u WHERE u.userName LIKE :prefix",
                    Long::class.java
                )
                    .setParameter("prefix", "$batchPrefix%")
                    .singleResult
                
                savedCount shouldBe 5L
            }
        }
        
        context("롤백 검증을 위한 특별 환경에서") {
            val previouslyCreatedUserPrefix = "rollback-verify-${UUID.randomUUID()}-"

            // 테스트 사용자 생성 및 영속화
            beforeContainer {
                // 새로운 HTTP 요청 헤더 설정
                setupMockHttpRequest()
                
                // 새로운 트랜잭션 컨텍스트에서 테스트 데이터 생성
                val users = (1..3).map { i ->
                    UserEntity(
                        userName = "$previouslyCreatedUserPrefix$i",
                        email = Email("$previouslyCreatedUserPrefix$i@example.com"),
                        password = Password.of("Test1234!"),
                        name = "Rollback Verification User $i"
                    )
                }
                
                users.forEach { entityManager.persist(it) }
                entityManager.flush()
            }
            
            it("롤백 후에는 이전에 생성된 모든 데이터가 존재하지 않아야 한다") {
                // 이전 테스트 케이스에서 생성한 데이터 조회
                val existingUsers = entityManager.createQuery(
                    "SELECT u FROM UserEntity u WHERE u.userName LIKE :prefix",
                    UserEntity::class.java
                )
                    .setParameter("prefix", "$previouslyCreatedUserPrefix%")
                    .resultList
                
                // 롤백이 정상적으로 수행되었다면 해당 접두사를 가진 사용자는 없어야 함
                existingUsers.shouldBeEmpty()
            }
        }
    }
}) {

    override fun extensions() = listOf(SpringExtension)
    companion object {
        /**
         * Auditing 정보 설정을 위한 HTTP 요청 헤더 모킹
         */
        private fun setupMockHttpRequest() {
            val mockRequest = mockk<HttpServletRequest>()
            every { mockRequest.getHeader(RequestHeaderUtils.REQUEST_SOURCE_HEADER) } returns RequestHeaderUtils.USER_APP_SOURCE
            every { mockRequest.getHeader(RequestHeaderUtils.USER_EMAIL_HEADER) } returns "test@example.com"
            every { mockRequest.getHeader(RequestHeaderUtils.USER_ID_HEADER) } returns "1"
            every { mockRequest.getHeader(RequestHeaderUtils.USER_ROLE_HEADER) } returns "ROLE_ADMIN"
            val attributes = ServletRequestAttributes(mockRequest)
            RequestContextHolder.setRequestAttributes(attributes)
        }
    }
} 