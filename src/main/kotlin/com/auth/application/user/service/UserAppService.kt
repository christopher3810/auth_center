package com.auth.application.user.service

import com.auth.domain.user.model.User
import com.auth.domain.user.service.UserDomainService
import com.auth.domain.user.value.Email
import com.auth.domain.user.value.Password
import com.auth.exception.InvalidCredentialsException
import com.auth.exception.UserNotFoundException
import com.vito.common.util.patternValidator.PatternValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 애플리케이션 서비스
 * 외부 요청을 적절한 도메인 서비스와 모델로 위임,
 * 트랜잭션 경계를 관리, 다양한 도메인 간의 협력 조정.
 */
@Service
class UserAppService(
    private val userDomainService: UserDomainService
) {
    /**
     * 사용자 등록
     *
     * 사용자 등록 요청을 도메인 서비스로 위임하여 새로운 사용자를 생성.
     *
     * @param username 사용자명
     * @param email 이메일
     * @param password 비밀번호
     * @param name 이름
     * @param phoneNumber 전화번호 (선택)
     * @return 생성된 사용자 도메인 객체
     */
    @Transactional
    fun registerUser(
        username: String,
        email: String,
        password: String,
        name: String,
        phoneNumber: String? = null
    ): User {
        return userDomainService.createUser(username, email, password, name, phoneNumber)
    }
    /**
     * 로그인 시 비밀번호·계정 상태 검증
     * 
     * @param usernameOrEmail 사용자명 또는 이메일
     * @param rawPassword 평문 비밀번호
     * @return 검증된 사용자 도메인 객체
     * @throws InvalidCredentialsException 인증 실패 시(비밀번호 불일치, 계정 상태 문제 등)
     */
    @Transactional(readOnly = true)
    fun validateLogin(usernameOrEmail: String, rawPassword: String): User = runCatching {
        // 이메일 또는 사용자명으로 사용자 조회
        val user = when {
            PatternValidator.isValidEmail(usernameOrEmail) -> userDomainService.findUserByEmail(Email(usernameOrEmail))
            else -> userDomainService.findUserByUsername(usernameOrEmail)
        }

        // 비밀번호 검증
        if (!user.password.matches(rawPassword)) {
            throw InvalidCredentialsException("아이디 또는 비밀번호가 맞지 않습니다.")
        }

        // 계정 상태 검증 
        user.takeIf { it.isLoginable() }
            ?: throw InvalidCredentialsException("현재 로그인할 수 없는 상태입니다: ${user.status}")

        user
    }.getOrElse { e ->
        when (e) {
            is UserNotFoundException -> throw InvalidCredentialsException("아이디 또는 비밀번호가 맞지 않습니다.")
            else -> throw e
        }
    }


    /**
     * ID로 사용자 조회
     * 
     * @param id 사용자 ID
     * @return 사용자 도메인 객체 또는 null
     */
    @Transactional(readOnly = true)
    fun getUserById(id: Long): User {
        return userDomainService.findUserById(id)
    }

    /**
     * 이메일로 사용자 조회
     *
     * 이메일 문자열을 Email 값 객체로 변환하여 도메인 서비스에 위임.
     *
     * @param email 이메일 문자열
     * @return 사용자 도메인 객체 또는 null
     */
    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): User {
        val emailObj = Email(email)
        return userDomainService.findUserByEmail(emailObj)
    }

    /**
     * 사용자명으로 사용자 조회
     *
     * @param username 사용자명
     * @return 사용자 도메인 객체를 포함한 Optional
     */
    @Transactional(readOnly = true)
    fun getUserByUsername(username: String): User {
        return userDomainService.findUserByUsername(username)
    }

    /**
     * 사용자 프로필 업데이트
     *
     * 사용자 도메인 객체의 프로필 정보를 업데이트하고 저장합니다.
     * 존재하지 않는 사용자일 경우 예외를 발생시킵니다.
     *
     * @param userId 사용자 ID
     * @param name 변경할 이름
     * @param phoneNumber 변경할 전화번호
     * @return 업데이트된 사용자 도메인 객체
     * @throws NoSuchElementException 사용자가 존재하지 않는 경우
     */
    @Transactional
    fun updateUserProfile(userId: Long, name: String, phoneNumber: String?): User =
        runCatching { userDomainService.findUserById(userId) }
            .getOrElse { throw UserNotFoundException.byId(userId, it) }
            .apply { update(name, phoneNumber) }
            .let { userDomainService.saveUser(it) }

    /**
     * 사용자 비밀번호 변경
     *
     * 현재 비밀번호를 확인하고 일치하는 경우에만 새 비밀번호로 변경합니다.
     *
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword 새 비밀번호
     * @return 비밀번호 변경 성공 여부
     * @throws NoSuchElementException 사용자가 존재하지 않는 경우
     */
    @Transactional
    fun changeUserPassword(userId: Long, currentPassword: String, newPassword: String): Boolean {
        val user = userDomainService.findUserById(userId)

        // 현재 비밀번호 확인 - 도메인 객체의 검증 기능 활용
        if (!user.password.matches(currentPassword)) {
            return false
        }

        // 새 비밀번호로 변경 - 도메인 객체의 행동 활용
        user.changePassword(Password.of(newPassword))
        userDomainService.saveUser(user)
        return true
    }

    /**
     * 사용자 활성화
     *
     * 비활성 상태의 사용자를 활성 상태로 변경합니다.
     *
     * @param userId 사용자 ID
     * @return 활성화 성공 여부 (이미 활성화된 경우 false)
     * @throws NoSuchElementException 사용자가 존재하지 않는 경우
     */
    @Transactional
    fun activateUser(userId: Long): Boolean {
        val user = userDomainService.findUserById(userId)

        if (!user.isActive()) {
            // 활성화 처리
            user.activate()
            userDomainService.saveUser(user)
            return true
        }
        return false
    }

    /**
     * 사용자 비활성화
     *
     * 활성 상태의 사용자를 비활성 상태로 변경합니다.
     *
     * @param userId 사용자 ID
     * @return 비활성화 성공 여부 (이미 비활성화된 경우 false)
     * @throws NoSuchElementException 사용자가 존재하지 않는 경우
     */
    @Transactional
    fun deactivateUser(userId: Long): Boolean {
        val user = userDomainService.findUserById(userId)

        if (user.isActive()) {
            // 비활성화 처리
            user.deactivate()
            userDomainService.saveUser(user)
            return true
        }
        return false
    }

    /**
     * 사용자 계정 잠금
     *
     * 사용자 계정을 잠금 상태로 변경합니다.
     *
     * @param userId 사용자 ID
     * @return 잠금 성공 여부
     * @throws NoSuchElementException 사용자가 존재하지 않는 경우
     */
    @Transactional
    fun lockUser(userId: Long): Boolean {
        val user = userDomainService.findUserById(userId)

        user.lock()
        userDomainService.saveUser(user)
        return true
    }

    /**
     * 사용자에게 역할 추가
     *
     * @param userId 사용자 ID
     * @param role 추가할 역할
     * @return 업데이트된 사용자 도메인 객체
     * @throws NoSuchElementException 사용자가 존재하지 않는 경우
     */
    @Transactional
    fun addRoleToUser(userId: Long, role: String): User {
        val user = userDomainService.findUserById(userId)

        user.addRole(role)
        return userDomainService.saveUser(user)
    }

    /**
     * 사용자에게서 역할 제거
     *
     * @param userId 사용자 ID
     * @param role 제거할 역할
     * @return 업데이트된 사용자 도메인 객체
     * @throws NoSuchElementException 사용자가 존재하지 않는 경우
     */
    @Transactional
    fun removeRoleFromUser(userId: Long, role: String): User {
        val user = userDomainService.findUserById(userId)

        user.removeRole(role)
        return userDomainService.saveUser(user)
    }

    /**
     * 사용자 역할 일괄 업데이트
     *
     * @param userId 사용자 ID
     * @param roles 새로운 역할 집합
     * @return 업데이트된 사용자 도메인 객체
     * @throws NoSuchElementException 사용자가 존재하지 않는 경우
     */
    @Transactional
    fun updateUserRoles(userId: Long, roles: Set<String>): User {
        val user = userDomainService.findUserById(userId)

        user.updateRoles(roles)
        return userDomainService.saveUser(user)
    }

    /**
     * 로그인 처리
     *
     * 사용자의 마지막 로그인 시간을 업데이트합니다.
     *
     * @param userId 사용자 ID
     * @return 업데이트된 사용자 도메인 객체
     * @throws NoSuchElementException 사용자가 존재하지 않는 경우
     */
    @Transactional
    fun recordUserLogin(userId: Long): User {
        val user = userDomainService.findUserById(userId)

        user.recordLogin()
        return userDomainService.saveUser(user)
    }

    /**
     * 전체 사용자 목록 조회
     *
     * @return 사용자 도메인 객체 목록
     */
    @Transactional(readOnly = true)
    fun getAllUsers(): List<User> {
        return userDomainService.findAllUsers()
    }

    /**
     * 사용자 삭제
     *
     * @param userId 사용자 ID
     * @throws NoSuchElementException 사용자가 존재하지 않는 경우
     */
    @Transactional
    fun deleteUser(userId: Long) {
        val user = userDomainService.findUserById(userId)

        userDomainService.deleteUser(user)
    }
} 