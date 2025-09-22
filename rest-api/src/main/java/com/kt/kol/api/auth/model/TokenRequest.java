package com.kt.kol.api.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JWT 토큰 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT 토큰 생성 요청")
public class TokenRequest {

    @Schema(description = "사용자명", example = "testuser")
    private String username;

    @Schema(description = "권한 목록", example = "[\"USER\", \"ADMIN\"]")
    private List<String> roles;

    @Schema(description = "토큰 만료 시간(초)", example = "3600", defaultValue = "86400")
    private Long expirationSeconds;
}