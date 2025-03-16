package com.auth.unit.service

import com.auth.infrastructure.security.token.JwtTokenAdaptor
import com.auth.domain.auth.model.TokenPurpose
import com.auth.domain.auth.model.TokenType
import com.auth.exception.InvalidTokenException
import com.auth.exception.TokenExpiredException
import com.auth.infrastructure.config.JwtConfig
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class JwtTokenAdaptorTest : DescribeSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    // 테스트 데이터 정의
    val testData = object {
        val email = "test@example.com"
        val userId = 123L
        val roles = setOf("ROLE_USER", "ROLE_ADMIN")
        val permissions = setOf("READ", "WRITE")
        val rolesString = roles.joinToString(",")
    }

    // 테스트용 JWT 설정
    val jwtConfig = JwtConfig("testSecretKeyForTokenProviderTestLongEnough",
        1.hours.inWholeMilliseconds,
        24.hours.inWholeMilliseconds)

    // 테스트용 키 - lazy로 필요할 때만 초기화
    val hmacKey by lazy {
        Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray(Charsets.UTF_8))
    }

    // 테스트용 토큰 생성 확장 함수
    fun createToken(
        subject: String = testData.email,
        expiresAt: Instant = Instant.now() + 1.hours.toJavaDuration(),
        claims: Map<String, Any> = emptyMap(),
        key: SecretKey = hmacKey
    ): String = Jwts.builder().apply {
        subject(subject)
        issuedAt(Date.from(Instant.now()))
        expiration(Date.from(expiresAt))
        claims.forEach { (name, value) -> claim(name, value) }
        signWith(key)
    }.compact()

    // 편의성을 위한 확장 함수
    infix fun String.shouldHaveParts(count: Int) =
        this.split(".").size shouldBe count

    describe("JwtTokenAdaptor") {
        val tokenAdapter by lazy { JwtTokenAdaptor(jwtConfig) }

        context("토큰 생성") {
            it("액세스 토큰 생성") {
                with(testData) {
                    val token = tokenAdapter.generateAccessTokenString(email, userId, roles, permissions)

                    token shouldHaveParts 3

                    tokenAdapter.getClaims(token).run {
                        subject shouldBe email
                        get("type") shouldBe "authorization"
                        get("userId") shouldBe userId
                        expiration.toInstant().isAfter(Instant.now()) shouldBe true
                    }
                }
            }

            it("리프레시 토큰 생성") {
                with(testData) {
                    val token = tokenAdapter.generateRefreshTokenString(email, userId)

                    token shouldHaveParts 3

                    tokenAdapter.getClaims(token).run {
                        subject shouldBe email
                        get("type") shouldBe "refresh"
                        get("userId") shouldBe userId
                        expiration.toInstant().isAfter(Instant.now()) shouldBe true
                    }
                }
            }

            it("일회용 토큰 생성") {
                with(testData) {
                    val purpose = TokenPurpose.EMAIL_VERIFICATION
                    val token = tokenAdapter.generateOneTimeTokenString(email, userId, purpose)

                    token shouldHaveParts 3

                    tokenAdapter.getClaims(token).run {
                        subject shouldBe email
                        get("purpose") shouldBe purpose.value
                        get("type") shouldBe "one_time"
                        get("userId") shouldBe userId
                    }
                }
            }
        }

        context("토큰 검증") {
            it("유효한 토큰") {
                val token = createToken()
                tokenAdapter.validateToken(token) shouldBe true
            }

            it("잘못된 서명의 토큰") {
                val validToken = createToken()
                val (header, payload, signature) = validToken.split(".")
                val tamperedToken = "$header.$payload.invalid${signature.substring(5)}"

                assertSoftly {
                    tokenAdapter.validateToken(tamperedToken) shouldBe false
                    shouldThrow<InvalidTokenException> {
                        tokenAdapter.getClaims(tamperedToken)
                    }
                }
            }

            it("만료된 토큰") {
                val expiredToken = createToken(
                    expiresAt = Instant.now() - 1.seconds.toJavaDuration()
                )

                assertSoftly {
                    tokenAdapter.validateToken(expiredToken) shouldBe false
                    shouldThrow<TokenExpiredException> {
                        tokenAdapter.getClaims(expiredToken)
                    }
                }
            }

            it("형식이 잘못된 토큰") {
                listOf(
                    "completely.invalid.token",
                    "no_dots_at_all",
                    "",
                    "just.one.dot"
                ).forEach { invalidToken ->
                    tokenAdapter.validateToken(invalidToken) shouldBe false
                }
            }
        }

        context("토큰 정보 추출") {
            it("모든 클레임 추출") {
                with(testData) {
                    val claims = mapOf(
                        "userId" to userId,
                        "roles" to rolesString,
                        "custom" to "value",
                        "type" to "authorization"
                    )

                    val token = createToken(claims = claims)

                    tokenAdapter.getClaims(token).apply {
                        subject shouldBe email
                        get("userId") shouldBe userId
                        get("roles") shouldBe rolesString
                        get("custom") shouldBe "value"
                        get("type") shouldBe "authorization"
                        id shouldBe null
                        issuedAt.shouldNotBeNull()
                        expiration.shouldNotBeNull()
                    }
                }
            }

            it("사용자 이메일(subject) 추출") {
                val token = createToken()
                tokenAdapter.getSubject(token) shouldBe testData.email
            }

            it("사용자 ID 추출") {
                val token = createToken(claims = mapOf("userId" to testData.userId))
                tokenAdapter.getUserId(token) shouldBe testData.userId
            }

            it("토큰 목적(purpose) 추출") {
                val purpose = TokenPurpose.EMAIL_VERIFICATION
                val token = createToken(claims = mapOf("purpose" to purpose.value))
                tokenAdapter.getPurpose(token) shouldBe purpose
            }

            it("토큰 타입 추출") {
                val token = createToken(claims = mapOf("type" to "authorization"))
                tokenAdapter.getTokenType(token) shouldBe TokenType.AUTHORIZATION
            }

            it("토큰 역할(roles) 추출") {
                val token = createToken(claims = mapOf("roles" to testData.rolesString))
                tokenAdapter.getRoles(token) shouldBe testData.roles
            }
        }
    }
})