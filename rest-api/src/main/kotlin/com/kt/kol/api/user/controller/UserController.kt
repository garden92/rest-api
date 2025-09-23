package com.kt.kol.api.user.controller

import com.kt.kol.api.user.model.UserDTO
import com.kt.kol.api.user.service.UserService
import com.kt.kol.common.model.PageDTO
import com.kt.kol.common.model.ResponseStdVO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

private val log = LoggerFactory.getLogger(UserController::class.java)

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User API", description = "사용자 관리 API")
class UserController(
    private val userService: UserService
) {

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "모든 사용자 조회", description = "페이지네이션을 적용하여 사용자를 조회합니다.")
    suspend fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseStdVO<PageDTO<UserDTO>> {
        return ResponseStdVO.success(userService.findAllUsersPaged(page, size))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "사용자 조회", description = "ID로 특정 사용자를 조회합니다.")
    suspend fun getUserById(@PathVariable id: Long): ResponseStdVO<UserDTO> {
        return ResponseStdVO.success(userService.findUserById(id))
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "사용자명으로 조회", description = "사용자명으로 사용자를 조회합니다.")
    suspend fun getUserByUsername(@PathVariable username: String): ResponseStdVO<UserDTO> {
        return ResponseStdVO.success(userService.findUserByUsername(username))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다.")
    suspend fun createUser(@RequestBody userDTO: UserDTO): ResponseStdVO<UserDTO> {
        return ResponseStdVO.success(userService.createUser(userDTO))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 수정", description = "기존 사용자 정보를 수정합니다.")
    suspend fun updateUser(
        @PathVariable id: Long,
        @RequestBody userDTO: UserDTO
    ): ResponseStdVO<UserDTO> {
        return ResponseStdVO.success(userService.updateUser(id, userDTO))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    suspend fun deleteUser(@PathVariable id: Long): ResponseStdVO<Any?> {
        userService.deleteUser(id)
        return ResponseStdVO.success(null)
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "사용자 검색", description = "이름으로 사용자를 검색합니다.")
    suspend fun searchUsers(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseStdVO<PageDTO<UserDTO>> {
        return ResponseStdVO.success(userService.searchUsersPaged(query, page, size))
    }
}