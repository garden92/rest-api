package com.kt.kol.api.auth.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * JWT 토큰 생성 응답 DTO
 */
@Schema(description = "JWT 토큰 생성 응답")
data class TokenResponse(
    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val token: String,

    @Schema(description = "토큰 만료 시간")
    val expiresAt: LocalDateTime,

    @Schema(description = "발급 대상 사용자명", example = "testuser")
    val username: String,

    @Schema(description = "사용자 권한", example = "[\"USER\", \"ADMIN\"]")
    val roles: List<String>,

    @Schema(description = "토큰 타입", example = "Bearer")
    val tokenType: String = "Bearer",

    @Schema(description = "사용법 안내", example = "스웨거 Authorize 버튼 클릭 후 'Bearer {token}' 형식으로 입력")
    val usage: String = "스웨거 Authorize 버튼 클릭 후 'Bearer {token}' 형식으로 입력하세요"
)