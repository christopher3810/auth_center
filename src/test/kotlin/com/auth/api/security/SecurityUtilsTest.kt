package com.auth.api.security

import com.auth.application.auth.dto.UserTokenInfo
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

class SecurityUtilsTest : DescribeSpec({
    
    isolationMode = IsolationMode.InstancePerLeaf

    val sut = SecurityUtils
    
    beforeTest {
        SecurityContextHolder.clearContext()
    }
    
    afterTest {
        SecurityContextHolder.clearContext()
    }
    
    describe("SecurityUtils 객체는") {
        context("setupAuthentication 메소드가 호출되었을 때") {
            val userInfo = UserTokenInfo(
                id = 1L,
                email = "user@example.com",
                roles = setOf("USER", "ADMIN"),
                additionalClaims = emptyMap()
            )
            
            beforeTest {
                sut.setupAuthentication(userInfo)
            }
            
            it("SecurityContext에 인증 정보를 설정해야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                authentication.shouldBeInstanceOf<UsernamePasswordAuthenticationToken>()
            }
            
            it("사용자 정보를 principal로 설정해야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                authentication.principal.shouldBe(userInfo)
            }
            
            it("사용자 역할에 기반한 권한을 설정해야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                val authorities = authentication.authorities.map { it.authority }
                
                authorities.shouldContainExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
            }
        }
        
        context("setupAdminAuthentication 메소드가 호출되었을 때") {
            val userInfo = UserTokenInfo(
                id = 1L,
                email = "user@example.com",
                roles = setOf("USER"),
                additionalClaims = emptyMap()
            )
            
            beforeTest {
                sut.setupAdminAuthentication(userInfo)
            }
            
            it("사용자 역할과 관리자 역할이 모두 포함되어야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                val authorities = authentication.authorities.map { it.authority }
                
                authorities.shouldContainExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
            }
        }
        
        context("setupReadOnlyAuthentication 메소드가 호출되었을 때") {
            val userInfo = UserTokenInfo(
                id = 1L,
                email = "user@example.com",
                roles = setOf("USER", "ADMIN"),
                additionalClaims = emptyMap()
            )
            
            beforeTest {
                sut.setupReadOnlyAuthentication(userInfo)
            }
            
            it("원래 역할과 상관없이 READER 역할만 가져야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                val authorities = authentication.authorities.map { it.authority }
                
                authorities.shouldContainExactlyInAnyOrder("ROLE_READER")
            }
        }
        
        context("setupAuthenticationWithRoles 메소드가 호출되었을 때") {
            val userInfo = UserTokenInfo(
                id = 1L,
                email = "user@example.com",
                roles = setOf("USER", "ADMIN"),
                additionalClaims = emptyMap()
            )
            val customRoles = setOf("MANAGER", "AUDITOR")
            
            beforeTest {
                sut.setupAuthenticationWithRoles(userInfo, customRoles)
            }
            
            it("원래 역할과 상관없이 커스텀 역할만 가져야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                val authorities = authentication.authorities.map { it.authority }
                
                authorities.shouldContainExactlyInAnyOrder("ROLE_MANAGER", "ROLE_AUDITOR")
            }
        }
        
        context("clearSecurityContext 메소드가 호출되었을 때") {
            val userInfo = UserTokenInfo(
                id = 1L,
                email = "user@example.com",
                roles = setOf("USER"),
                additionalClaims = emptyMap()
            )
            
            beforeTest {
                sut.setupAuthentication(userInfo)

                sut.clearSecurityContext()
            }
            
            it("SecurityContext에서 인증 정보가 제거되어야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                authentication.shouldBe(null)
            }
        }
        
        context("isAuthenticated 메소드가 호출되었을 때") {
            context("인증된 컨텍스트가 있는 경우") {
                val userInfo = UserTokenInfo(
                    id = 1L,
                    email = "user@example.com",
                    roles = setOf("USER"),
                    additionalClaims = emptyMap()
                )
                
                beforeTest {
                    sut.setupAuthentication(userInfo)
                }
                
                it("true를 반환해야 한다") {
                    sut.isAuthenticated().shouldBe(true)
                }
            }
            
            context("인증되지 않은 컨텍스트인 경우") {
                beforeTest {
                    SecurityContextHolder.clearContext()
                }
                
                it("false를 반환해야 한다") {
                    sut.isAuthenticated().shouldBe(false)
                }
            }
        }
        
        context("ROLE_ 접두사가 없는 역할이 주어졌을 때") {
            val userInfo = UserTokenInfo(
                id = 1L,
                email = "user@example.com",
                roles = setOf("ADMIN"),
                additionalClaims = emptyMap()
            )
            
            beforeTest {
                sut.setupAuthentication(userInfo)
            }
            
            it("ROLE_ 접두사를 자동으로 추가해야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                val authorities = authentication.authorities.map { it.authority }
                
                authorities.shouldContainExactlyInAnyOrder("ROLE_ADMIN")
            }
        }
        
        context("이미 ROLE_ 접두사가 있는 역할이 주어졌을 때") {
            val userInfo = UserTokenInfo(
                id = 1L,
                email = "user@example.com",
                roles = setOf("ROLE_ADMIN"),
                additionalClaims = emptyMap()
            )
            
            beforeTest {
                sut.setupAuthentication(userInfo)
            }
            
            it("접두사를 중복해서 추가하지 않아야 한다") {
                val authentication = SecurityContextHolder.getContext().authentication
                val authorities = authentication.authorities.map { it.authority }
                
                authorities.shouldContainExactlyInAnyOrder("ROLE_ADMIN")
            }
        }
    }
}) 