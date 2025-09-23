package com.kt.kol.api.auth.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * JWT 토큰 생성 요청 DTO
 */
@Schema(description = "JWT 토큰 생성 요청")
data class TokenRequest(
    @Schema(description = "사용자명", example = "testuser")
    val username: String? = null,

    @Schema(description = "권한 목록", example = "[\"USER\", \"ADMIN\"]")
    val roles: List<String>? = null,

    @Schema(description = "토큰 만료 시간(초)", example = "3600", defaultValue = "86400")
    val expirationSeconds: Long? = null
)