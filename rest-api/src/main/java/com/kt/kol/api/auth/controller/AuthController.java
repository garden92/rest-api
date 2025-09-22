package com.kt.kol.api.auth.controller;

import com.kt.kol.api.auth.model.TokenRequest;
import com.kt.kol.api.auth.model.TokenResponse;
import com.kt.kol.common.model.ResponseStdVO;
import com.kt.kol.common.service.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "인증 관련 API (테스트용)")
public class AuthController {

    private final JwtTokenService jwtTokenService;

    @Value("${jwt.expiration}")
    private long defaultExpiration;

    @PostMapping("/token")
    @Operation(summary = "JWT 토큰 생성", description = "테스트용 JWT 토큰을 생성합니다. 스웨거 인증에 사용할 수 있습니다.")
    public Mono<ResponseStdVO<TokenResponse>> generateToken(@RequestBody TokenRequest request) {
        try {
            String username = request.getUsername() != null ? request.getUsername() : "testuser";
            List<String> roles = request.getRoles() != null && !request.getRoles().isEmpty() 
                ? request.getRoles() 
                : List.of("USER");

            String token = jwtTokenService.generateTestToken(username, roles);
            
            long expiration = request.getExpirationSeconds() != null 
                ? request.getExpirationSeconds() 
                : defaultExpiration;
            
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiration);

            TokenResponse response = new TokenResponse(token, expiresAt, username, roles);
            
            log.info("Generated test token for user: {}, roles: {}", username, roles);
            
            return Mono.just(ResponseStdVO.success(response));
        } catch (Exception e) {
            log.error("Failed to generate token", e);
            return Mono.just(ResponseStdVO.<TokenResponse>error("토큰 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/token/user")
    @Operation(summary = "USER 권한 토큰 생성", description = "USER 권한을 가진 테스트 토큰을 즉시 생성합니다.")
    public Mono<ResponseStdVO<TokenResponse>> generateUserToken() {
        try {
            String username = "testuser";
            List<String> roles = List.of("USER");
            String token = jwtTokenService.generateUserToken(username);
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(defaultExpiration);

            TokenResponse response = new TokenResponse(token, expiresAt, username, roles);
            
            log.info("Generated USER token for: {}", username);
            
            return Mono.just(ResponseStdVO.success(response));
        } catch (Exception e) {
            log.error("Failed to generate user token", e);
            return Mono.just(ResponseStdVO.<TokenResponse>error("USER 토큰 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/token/admin")
    @Operation(summary = "ADMIN 권한 토큰 생성", description = "USER, ADMIN 권한을 가진 테스트 토큰을 즉시 생성합니다.")
    public Mono<ResponseStdVO<TokenResponse>> generateAdminToken() {
        try {
            String username = "testadmin";
            List<String> roles = List.of("USER", "ADMIN");
            String token = jwtTokenService.generateAdminToken(username);
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(defaultExpiration);

            TokenResponse response = new TokenResponse(token, expiresAt, username, roles);
            
            log.info("Generated ADMIN token for: {}", username);
            
            return Mono.just(ResponseStdVO.success(response));
        } catch (Exception e) {
            log.error("Failed to generate admin token", e);
            return Mono.just(ResponseStdVO.<TokenResponse>error("ADMIN 토큰 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/token/validate")
    @Operation(summary = "토큰 검증", description = "JWT 토큰의 유효성을 검증하고 클레임 정보를 반환합니다.")
    public Mono<ResponseStdVO<Object>> validateToken(@RequestParam String token) {
        try {
            if (jwtTokenService.validateToken(token)) {
                var claims = jwtTokenService.getClaimsFromToken(token);
                log.info("Token validation successful for subject: {}", claims.getSubject());
                return Mono.just(ResponseStdVO.success(claims));
            } else {
                return Mono.just(ResponseStdVO.<Object>error("유효하지 않은 토큰입니다"));
            }
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return Mono.just(ResponseStdVO.<Object>error("토큰 검증 실패: " + e.getMessage()));
        }
    }
}