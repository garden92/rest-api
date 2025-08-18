package com.kt.kol.api.encryption.util;

import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 채널별 암호화 키 생성 유틸리티 테스트
 * 실제 운영에서 사용할 키 생성 및 관리 방식 예제
 */
@DisplayName("채널 키 생성기 테스트")
class ChannelKeyGeneratorTest {

    @Test
    @DisplayName("채널별 AES-256 키 생성 및 관리")
    void generateChannelKeysTest() {
        // Given - 채널 목록
        String[] channels = {"AI", "KI"};
        
        System.out.println("\n=== 채널별 AES-256 키 생성 결과 ===");
        System.out.println("생성시간: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("-".repeat(80));
        
        Map<String, ChannelKeyInfo> channelKeys = new HashMap<>();
        
        // When - 각 채널별 키 생성
        for (String channelId : channels) {
            ChannelKeyInfo keyInfo = generateChannelKey(channelId);
            channelKeys.put(channelId, keyInfo);
            
            System.out.printf("채널ID: %-12s | 키ID: %-20s%n", channelId, keyInfo.getKeyId());
            System.out.printf("알고리즘: %-10s | 버전: %-5d | 생성일시: %s%n", 
                keyInfo.getAlgorithm(), keyInfo.getKeyVersion(), keyInfo.getCreatedAt());
            System.out.printf("키값(Base64): %s%n", keyInfo.getKeyMaterial());
            System.out.println("-".repeat(80));
        }
        
        // Then - 검증
        assertEquals(2, channelKeys.size());
        
        // 각 채널의 키가 서로 다른지 확인
        for (int i = 0; i < channels.length; i++) {
            for (int j = i + 1; j < channels.length; j++) {
                String key1 = channelKeys.get(channels[i]).getKeyMaterial();
                String key2 = channelKeys.get(channels[j]).getKeyMaterial();
                assertNotEquals(key1, key2, "채널별 키가 동일하면 안됩니다");
            }
        }
        
        // 키 길이 검증 (Base64 인코딩된 32바이트는 44자리)
        channelKeys.values().forEach(keyInfo -> {
            byte[] keyBytes = Base64.getDecoder().decode(keyInfo.getKeyMaterial());
            assertEquals(32, keyBytes.length, "AES-256 키는 32바이트여야 합니다");
        });
    }

    @Test
    @DisplayName("키 버전 관리 시스템 테스트")
    void keyVersionManagementTest() {
        String channelId = "AI";
        
        System.out.println("\n=== 키 버전 관리 시스템 테스트 ===");
        
        // 버전 1 키 생성
        ChannelKeyInfo keyV1 = generateChannelKey(channelId, 1);
        System.out.println("Version 1 키: " + keyV1.getKeyId());
        System.out.println("키값: " + keyV1.getKeyMaterial());
        
        // 버전 2 키 생성 (키 교체 시)
        ChannelKeyInfo keyV2 = generateChannelKey(channelId, 2);
        System.out.println("Version 2 키: " + keyV2.getKeyId());
        System.out.println("키값: " + keyV2.getKeyMaterial());
        
        // 검증
        assertNotEquals(keyV1.getKeyMaterial(), keyV2.getKeyMaterial());
        assertEquals(1, keyV1.getKeyVersion());
        assertEquals(2, keyV2.getKeyVersion());
        assertEquals("KEY_AI_V1", keyV1.getKeyId());
        assertEquals("KEY_AI_V2", keyV2.getKeyId());
    }

    @Test
    @DisplayName("키 교환용 RSA 키페어 생성 테스트")
    void rsaKeyPairGenerationTest() {
        System.out.println("\n=== RSA-2048 키페어 생성 (키 교환용) ===");
        
        try {
            // RSA-2048 키페어 생성
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            
            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            
            System.out.println("공개키 (채널에 전달):");
            System.out.println(publicKey);
            System.out.println("\n개인키 (서버 보관):");
            System.out.println(privateKey);
            
            // 키 길이 검증
            assertTrue(publicKey.length() > 300, "RSA 공개키가 너무 짧습니다");
            assertTrue(privateKey.length() > 1000, "RSA 개인키가 너무 짧습니다");
            
        } catch (Exception e) {
            fail("RSA 키페어 생성 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("키 전달 JSON 포맷 생성 테스트")
    void keyDeliveryJsonFormatTest() {
        // Given
        String channelId = "AI";
        ChannelKeyInfo keyInfo = generateChannelKey(channelId);
        
        // When - 키 전달용 JSON 포맷 생성
        String keyDeliveryJson = createKeyDeliveryJson(keyInfo);
        
        System.out.println("\n=== 채널 키 전달 JSON 포맷 ===");
        System.out.println(keyDeliveryJson);
        
        // Then
        assertTrue(keyDeliveryJson.contains("\"keyId\""));
        assertTrue(keyDeliveryJson.contains("\"algorithm\""));
        assertTrue(keyDeliveryJson.contains("\"keyMaterial\""));
        assertTrue(keyDeliveryJson.contains("AES-256-GCM"));
    }

    /**
     * 채널별 AES-256 키 생성
     */
    private ChannelKeyInfo generateChannelKey(String channelId) {
        return generateChannelKey(channelId, 1);
    }
    
    /**
     * 채널별 AES-256 키 생성 (버전 지정)
     */
    private ChannelKeyInfo generateChannelKey(String channelId, int version) {
        try {
            // AES-256 키 생성
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, SecureRandom.getInstanceStrong());
            SecretKey secretKey = keyGenerator.generateKey();
            
            String keyId = String.format("KEY_%s_V%d", channelId, version);
            String keyMaterial = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            return new ChannelKeyInfo(keyId, "AES-256-GCM", version, keyMaterial, createdAt);
            
        } catch (Exception e) {
            throw new RuntimeException("키 생성 실패", e);
        }
    }
    
    /**
     * 키 전달용 JSON 포맷 생성
     */
    private String createKeyDeliveryJson(ChannelKeyInfo keyInfo) {
        return String.format("""
            {
              "keyId": "%s",
              "algorithm": "%s",
              "keyVersion": %d,
              "keyMaterial": "%s",
              "createdAt": "%s",
              "usage": "FIELD_ENCRYPTION",
              "instructions": {
                "algorithm": "AES-256-GCM",
                "keySize": "256 bits",
                "ivLength": "12 bytes (96 bits)",
                "tagLength": "16 bytes (128 bits)",
                "encoding": "Base64"
              }
            }""", 
            keyInfo.getKeyId(),
            keyInfo.getAlgorithm(),
            keyInfo.getKeyVersion(),
            keyInfo.getKeyMaterial(),
            keyInfo.getCreatedAt()
        );
    }

    /**
     * 채널 키 정보 DTO
     */
    static class ChannelKeyInfo {
        private final String keyId;
        private final String algorithm;
        private final Integer keyVersion;
        private final String keyMaterial;
        private final String createdAt;
        
        public ChannelKeyInfo(String keyId, String algorithm, Integer keyVersion, 
                             String keyMaterial, String createdAt) {
            this.keyId = keyId;
            this.algorithm = algorithm;
            this.keyVersion = keyVersion;
            this.keyMaterial = keyMaterial;
            this.createdAt = createdAt;
        }
        
        // Getters
        public String getKeyId() { return keyId; }
        public String getAlgorithm() { return algorithm; }
        public Integer getKeyVersion() { return keyVersion; }
        public String getKeyMaterial() { return keyMaterial; }
        public String getCreatedAt() { return createdAt; }
    }
}