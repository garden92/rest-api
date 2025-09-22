package com.kt.kol.common.service;

import com.kt.kol.common.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT 토큰 생성 및 검증을 담당하는 중앙화된 서비스
 * 모든 JWT 관련 로직을 한 곳에서 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtConfig jwtConfig;

    /**
     * JWT 토큰 생성 (범용)
     */
    public String generateToken(String subject, List<String> roles, Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtConfig.getExpiration(), ChronoUnit.SECONDS);

        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(JwtConfig.ROLES_CLAIM, roles)
                .signWith(jwtConfig.getSecretKey(), JwtConfig.SIGNATURE_ALGORITHM);

        if (additionalClaims != null) {
            additionalClaims.forEach(builder::claim);
        }

        return builder.compact();
    }

    /**
     * 기본 사용자 토큰 생성 (USER 권한)
     */
    public String generateUserToken(String username) {
        return generateToken(username, List.of("USER"), Map.of(JwtConfig.USERNAME_CLAIM, username));
    }

    /**
     * 관리자 토큰 생성 (USER, ADMIN 권한)
     */
    public String generateAdminToken(String username) {
        return generateToken(username, List.of("USER", "ADMIN"), Map.of(JwtConfig.USERNAME_CLAIM, username));
    }

    /**
     * 테스트용 토큰 생성
     */
    public String generateTestToken(String username, List<String> roles) {
        return generateToken(username, roles, Map.of(
                JwtConfig.USERNAME_CLAIM, username,
                "test", true,
                "issuer", "kol-rest-api-test"
        ));
    }

    /**
     * 토큰에서 클레임 추출
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 사용자명 추출
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(JwtConfig.USERNAME_CLAIM, String.class);
    }

    /**
     * 토큰에서 권한 목록 추출
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(JwtConfig.ROLES_CLAIM, List.class);
    }
}