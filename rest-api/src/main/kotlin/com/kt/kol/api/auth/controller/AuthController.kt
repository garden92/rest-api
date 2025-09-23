package com.kt.kol.api.auth.controller

import com.kt.kol.api.auth.model.TokenRequest
import com.kt.kol.api.auth.model.TokenResponse
import com.kt.kol.common.model.ResponseStdVO
import com.kt.kol.common.service.JwtTokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

private val log = LoggerFactory.getLogger(AuthController::class.java)

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth API", description = "인증 관련 API (테스트용)")
class AuthController(
    private val jwtTokenService: JwtTokenService,
    @Value("\${jwt.expiration}") private val defaultExpiration: Long
) {

    @PostMapping("/token")
    @Operation(summary = "JWT 토큰 생성", description = "테스트용 JWT 토큰을 생성합니다. 스웨거 인증에 사용할 수 있습니다.")
    suspend fun generateToken(@RequestBody request: TokenRequest): ResponseStdVO<TokenResponse> {
        return try {
            val username = request.username ?: "testuser"
            val roles = request.roles?.takeIf { it.isNotEmpty() } ?: listOf("USER")

            val token = jwtTokenService.generateTestToken(username, roles)

            val expiration = request.expirationSeconds ?: defaultExpiration
            val expiresAt = LocalDateTime.now().plusSeconds(expiration)

            val response = TokenResponse(token, expiresAt, username, roles)

            log.info("Generated test token for user: $username, roles: $roles")

            ResponseStdVO.success(response)
        } catch (e: Exception) {
            log.error("Failed to generate token", e)
            ResponseStdVO.error("토큰 생성에 실패했습니다: ${e.message}")
        }
    }

    @GetMapping("/token/user")
    @Operation(summary = "USER 권한 토큰 생성", description = "USER 권한을 가진 테스트 토큰을 즉시 생성합니다.")
    suspend fun generateUserToken(): ResponseStdVO<TokenResponse> {
        return try {
            val username = "testuser"
            val roles = listOf("USER")
            val token = jwtTokenService.generateUserToken(username)
            val expiresAt = LocalDateTime.now().plusSeconds(defaultExpiration)

            val response = TokenResponse(token, expiresAt, username, roles)

            log.info("Generated USER token for: $username")

            ResponseStdVO.success(response)
        } catch (e: Exception) {
            log.error("Failed to generate user token", e)
            ResponseStdVO.error("USER 토큰 생성에 실패했습니다: ${e.message}")
        }
    }

    @GetMapping("/token/admin")
    @Operation(summary = "ADMIN 권한 토큰 생성", description = "USER, ADMIN 권한을 가진 테스트 토큰을 즉시 생성합니다.")
    suspend fun generateAdminToken(): ResponseStdVO<TokenResponse> {
        return try {
            val username = "testadmin"
            val roles = listOf("USER", "ADMIN")
            val token = jwtTokenService.generateAdminToken(username)
            val expiresAt = LocalDateTime.now().plusSeconds(defaultExpiration)

            val response = TokenResponse(token, expiresAt, username, roles)

            log.info("Generated ADMIN token for: $username")

            ResponseStdVO.success(response)
        } catch (e: Exception) {
            log.error("Failed to generate admin token", e)
            ResponseStdVO.error("ADMIN 토큰 생성에 실패했습니다: ${e.message}")
        }
    }

    @PostMapping("/token/validate")
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증하고 클레임 정보를 반환합니다.")
    suspend fun validateToken(@RequestParam token: String): ResponseStdVO<Any> {
        return try {
            if (jwtTokenService.validateToken(token)) {
                val claims = jwtTokenService.getClaimsFromToken(token)
                log.info("Token validation successful for subject: ${claims.subject}")
                ResponseStdVO.success(claims)
            } else {
                ResponseStdVO.error("유효하지 않은 토큰입니다")
            }
        } catch (e: Exception) {
            log.error("Token validation failed", e)
            ResponseStdVO.error("토큰 검증 실패: ${e.message}")
        }
    }
}