package com.auth.controller.dto.request

import jakarta.validation.constraints.NotBlank

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    @field:NotBlank(message = "Username cannot be blank")
    val username: String,
    
    @field:NotBlank(message = "Password cannot be blank")
    val password: String
) 