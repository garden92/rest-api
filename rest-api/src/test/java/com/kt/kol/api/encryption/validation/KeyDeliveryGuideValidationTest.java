package com.kt.kol.api.encryption.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * KEY_DELIVERY_GUIDE.md에 작성된 암호화 방식 검증 테스트
 * "서정원" 문자열로 AI/KI 채널별 암복호화 실제 검증
 */
@DisplayName("키 전달 가이드 검증 테스트")
class KeyDeliveryGuideValidationTest {

    private static final String TEST_NAME = "서정원";
    
    // 실제 생성된 AI/KI 채널 키 (이전 테스트에서 생성됨)
    private static final String AI_CHANNEL_KEY = "nRDvh0YK+3X48rPtMQvp++j9qIlPBCEcpm16fKpEJ/g=";
    private static final String KI_CHANNEL_KEY = "oREoEDinr0hSXtZ/g2hD6dm4TpbdMAJiFh3E6k5oVtA=";

    @Test
    @DisplayName("AI 채널 - 서정원 암복호화 검증")
    void testAIChannelEncryptionDecryption() {
        System.out.println("\n=== AI 채널 암복호화 검증 ===");
        
        // Given
        String originalName = TEST_NAME;
        String channelKey = AI_CHANNEL_KEY;
        
        System.out.println("채널: AI");
        System.out.println("원문: " + originalName);
        System.out.println("키: " + channelKey);
        
        // When - KEY_DELIVERY_GUIDE.md 방식으로 암호화
        String encryptedName = AESGCMUtil.encryptField(originalName, channelKey);
        System.out.println("암호문: " + encryptedName);
        
        // When - KEY_DELIVERY_GUIDE.md 방식으로 복호화
        String decryptedName = AESGCMUtil.decryptField(encryptedName, channelKey);
        System.out.println("복호문: " + decryptedName);
        
        // Then - 검증
        assertEquals(originalName, decryptedName, "AI 채널 복호화 실패");
        assertNotEquals(originalName, encryptedName, "암호화가 되지 않음");
        assertTrue(encryptedName.length() > originalName.length(), "암호문이 원문보다 짧음");
        
        // Base64 형식 검증
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encryptedName), "암호문이 Base64 형식이 아님");
        
        System.out.println("✅ AI 채널 암복호화 성공!");
    }

    @Test
    @DisplayName("KI 채널 - 서정원 암복호화 검증")
    void testKIChannelEncryptionDecryption() {
        System.out.println("\n=== KI 채널 암복호화 검증 ===");
        
        // Given
        String originalName = TEST_NAME;
        String channelKey = KI_CHANNEL_KEY;
        
        System.out.println("채널: KI");
        System.out.println("원문: " + originalName);
        System.out.println("키: " + channelKey);
        
        // When - KEY_DELIVERY_GUIDE.md 방식으로 암호화
        String encryptedName = AESGCMUtil.encryptField(originalName, channelKey);
        System.out.println("암호문: " + encryptedName);
        
        // When - KEY_DELIVERY_GUIDE.md 방식으로 복호화
        String decryptedName = AESGCMUtil.decryptField(encryptedName, channelKey);
        System.out.println("복호문: " + decryptedName);
        
        // Then - 검증
        assertEquals(originalName, decryptedName, "KI 채널 복호화 실패");
        assertNotEquals(originalName, encryptedName, "암호화가 되지 않음");
        assertTrue(encryptedName.length() > originalName.length(), "암호문이 원문보다 짧음");
        
        // Base64 형식 검증
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encryptedName), "암호문이 Base64 형식이 아님");
        
        System.out.println("✅ KI 채널 암복호화 성공!");
    }

    @Test
    @DisplayName("채널별 키 독립성 검증 - 다른 채널 키로 복호화 실패")
    void testChannelKeyIndependence() {
        System.out.println("\n=== 채널 키 독립성 검증 ===");
        
        // Given - AI 채널로 암호화
        String originalName = TEST_NAME;
        String encryptedWithAI = AESGCMUtil.encryptField(originalName, AI_CHANNEL_KEY);
        
        System.out.println("AI 채널로 암호화: " + encryptedWithAI);
        
        // When & Then - KI 채널 키로 복호화 시도 (실패해야 함)
        assertThrows(RuntimeException.class, () -> {
            AESGCMUtil.decryptField(encryptedWithAI, KI_CHANNEL_KEY);
        }, "다른 채널 키로 복호화가 성공하면 안됨");
        
        System.out.println("✅ 채널 키 독립성 검증 성공!");
    }

    @Test
    @DisplayName("여러번 암호화 시 다른 암호문 생성 검증 (IV 랜덤성)")
    void testIVRandomness() {
        System.out.println("\n=== IV 랜덤성 검증 ===");
        
        // Given
        String originalName = TEST_NAME;
        String channelKey = AI_CHANNEL_KEY;
        
        // When - 동일한 평문을 여러번 암호화
        String encrypted1 = AESGCMUtil.encryptField(originalName, channelKey);
        String encrypted2 = AESGCMUtil.encryptField(originalName, channelKey);
        String encrypted3 = AESGCMUtil.encryptField(originalName, channelKey);
        
        System.out.println("1차 암호화: " + encrypted1);
        System.out.println("2차 암호화: " + encrypted2);
        System.out.println("3차 암호화: " + encrypted3);
        
        // Then - 모두 다른 암호문이어야 함
        assertNotEquals(encrypted1, encrypted2, "동일한 암호문 생성됨 (IV 랜덤성 문제)");
        assertNotEquals(encrypted2, encrypted3, "동일한 암호문 생성됨 (IV 랜덤성 문제)");
        assertNotEquals(encrypted1, encrypted3, "동일한 암호문 생성됨 (IV 랜덤성 문제)");
        
        // 모든 암호문이 올바르게 복호화되는지 확인
        assertEquals(originalName, AESGCMUtil.decryptField(encrypted1, channelKey));
        assertEquals(originalName, AESGCMUtil.decryptField(encrypted2, channelKey));
        assertEquals(originalName, AESGCMUtil.decryptField(encrypted3, channelKey));
        
        System.out.println("✅ IV 랜덤성 검증 성공!");
    }

    @Test
    @DisplayName("키 길이 검증 - 32바이트 AES-256")
    void testKeyLength() {
        System.out.println("\n=== 키 길이 검증 ===");
        
        // Given & When
        byte[] aiKeyBytes = Base64.getDecoder().decode(AI_CHANNEL_KEY);
        byte[] kiKeyBytes = Base64.getDecoder().decode(KI_CHANNEL_KEY);
        
        System.out.println("AI 채널 키 길이: " + aiKeyBytes.length + " bytes");
        System.out.println("KI 채널 키 길이: " + kiKeyBytes.length + " bytes");
        
        // Then - 32바이트 (256비트) 검증
        assertEquals(32, aiKeyBytes.length, "AI 채널 키가 32바이트가 아님");
        assertEquals(32, kiKeyBytes.length, "KI 채널 키가 32바이트가 아님");
        
        System.out.println("✅ 키 길이 검증 성공!");
    }

    /**
     * KEY_DELIVERY_GUIDE.md에 명시된 AES-GCM 암호화 유틸리티
     * 가이드 문서와 정확히 동일한 구현
     */
    static class AESGCMUtil {
        
        private static final String ALGORITHM = "AES";
        private static final String TRANSFORMATION = "AES/GCM/NoPadding";
        private static final int GCM_IV_LENGTH = 12;
        private static final int GCM_TAG_LENGTH = 16;
        
        /**
         * 필드 암호화 (KEY_DELIVERY_GUIDE.md 방식)
         */
        public static String encryptField(String plainText, String base64Key) {
            try {
                // Base64 키 디코딩
                byte[] keyBytes = Base64.getDecoder().decode(base64Key);
                SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
                
                // Cipher 초기화
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                
                // 랜덤 IV 생성
                byte[] iv = new byte[GCM_IV_LENGTH];
                SecureRandom.getInstanceStrong().nextBytes(iv);
                
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
                
                // 암호화
                byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
                
                // IV + 암호문 결합
                byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + cipherText.length];
                System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
                System.arraycopy(cipherText, 0, encryptedWithIv, GCM_IV_LENGTH, cipherText.length);
                
                return Base64.getEncoder().encodeToString(encryptedWithIv);
                
            } catch (Exception e) {
                throw new RuntimeException("암호화 실패", e);
            }
        }
        
        /**
         * 필드 복호화 (KEY_DELIVERY_GUIDE.md 방식)
         */
        public static String decryptField(String encryptedText, String base64Key) {
            try {
                // Base64 암호문 디코딩
                byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);
                
                // IV와 암호문 분리
                byte[] iv = new byte[GCM_IV_LENGTH];
                byte[] cipherText = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
                
                System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
                System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, cipherText, 0, cipherText.length);
                
                // Base64 키 디코딩
                byte[] keyBytes = Base64.getDecoder().decode(base64Key);
                SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
                
                // Cipher 초기화
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
                
                // 복호화
                byte[] plainBytes = cipher.doFinal(cipherText);
                return new String(plainBytes, StandardCharsets.UTF_8);
                
            } catch (Exception e) {
                throw new RuntimeException("복호화 실패", e);
            }
        }
    }
}