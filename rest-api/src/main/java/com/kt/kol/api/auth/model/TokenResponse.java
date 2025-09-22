package com.kt.kol.api.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JWT 토큰 생성 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT 토큰 생성 응답")
public class TokenResponse {

    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "토큰 만료 시간")
    private LocalDateTime expiresAt;

    @Schema(description = "발급 대상 사용자명", example = "testuser")
    private String username;

    @Schema(description = "사용자 권한", example = "[\"USER\", \"ADMIN\"]")
    private List<String> roles;

    @Schema(description = "사용법 안내", example = "스웨거 Authorize 버튼 클릭 후 'Bearer {token}' 형식으로 입력")
    private String usage = "스웨거 Authorize 버튼 클릭 후 'Bearer {token}' 형식으로 입력하세요";

    public TokenResponse(String token, LocalDateTime expiresAt, String username, List<String> roles) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.username = username;
        this.roles = roles;
    }
}