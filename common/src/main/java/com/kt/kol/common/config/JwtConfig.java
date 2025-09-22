package com.kt.kol.common.config;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * JWT 관련 중앙화된 설정 클래스
 * 토큰 생성과 검증에서 일관된 설정을 보장합니다.
 */
@Component
@Getter
public class JwtConfig {

    // JWT 알고리즘 - 한 곳에서 관리
    public static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;
    public static final String HMAC_ALGORITHM = "HmacSHA256";
    
    // 토큰 클레임 키
    public static final String ROLES_CLAIM = "roles";
    public static final String USERNAME_CLAIM = "username";
    
    private final String secret;
    private final long expiration;
    private final SecretKey secretKey;

    public JwtConfig(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expiration}") long expiration) {
        this.secret = secret;
        this.expiration = expiration;
        this.secretKey = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), 
            HMAC_ALGORITHM
        );
    }

    /**
     * JWT 서명용 SecretKey 반환
     */
    public SecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * Spring Security용 SecretKey 바이트 배열 반환
     */
    public byte[] getSecretKeyBytes() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}