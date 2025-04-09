package com.auth.api.rest.auth

import com.auth.api.docs.annotations.ApiForbiddenError
import com.auth.api.docs.annotations.ApiServerError
import com.auth.api.docs.annotations.ApiUserNotFoundError
import com.auth.api.security.annotation.AdminOnly
import com.auth.application.user.service.UserAccountAppService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Admin",
    description = "Admin 기능을 제공하는 Api 계정 활성화, 비활성화, 잠금 기능을 제공합니다.",
)
@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val userAccountAppService: UserAccountAppService,
) {
    @AdminOnly
    @Operation(
        summary = "계정 활성화",
        description = "지정된 사용자 계정을 활성화 상태로 변경합니다.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "활성화 성공",
        content = [
            Content(
                mediaType = "application/json",
                examples = [
                    ExampleObject(
                        name = "success",
                        summary = "계정 활성화 성공 응답",
                        value = "true",
                    ),
                ],
            ),
        ],
    )
    @ApiForbiddenError
    @ApiUserNotFoundError
    @ApiServerError
    @PatchMapping("v1/users/{userId}/activate")
    fun activateUser(
        @PathVariable userId: Long,
    ): Boolean = userAccountAppService.activateUser(userId)

    @AdminOnly
    @Operation(
        summary = "계정 비활성화",
        description = "지정된 사용자 계정을 비활성화 상태로 변경합니다.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "비활성화 성공",
        content = [
            Content(
                mediaType = "application/json",
                examples = [
                    ExampleObject(
                        name = "success",
                        summary = "계정 비활성화 성공 응답",
                        value = "true",
                    ),
                ],
            ),
        ],
    )
    @ApiForbiddenError
    @ApiUserNotFoundError
    @ApiServerError
    @PatchMapping("v1/users/{userId}/deactivate")
    fun deactivateUser(
        @PathVariable userId: Long,
    ): Boolean = userAccountAppService.deactivateUser(userId)

    @AdminOnly
    @Operation(
        summary = "계정 잠금",
        description = "지정된 사용자 계정을 잠금 상태로 변경합니다.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "잠금 성공",
        content = [
            Content(
                mediaType = "application/json",
                examples = [
                    ExampleObject(
                        name = "success",
                        summary = "계정 잠금 성공 응답",
                        value = "true",
                    ),
                ],
            ),
        ],
    )
    @ApiForbiddenError
    @ApiUserNotFoundError
    @ApiServerError
    @PatchMapping("v1/users/{userId}/lock")
    fun lockUser(
        @PathVariable userId: Long,
    ): Boolean = userAccountAppService.lockUser(userId)
}
