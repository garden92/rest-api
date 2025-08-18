package com.kt.kol.api.encryption.service;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AES-256-GCM 암호화/복호화 서비스 테스트
 * 실제 채널에 전달할 키 생성 및 사용법 예제 포함
 */
@DisplayName("AES-256-GCM 암호화 서비스 테스트")
class AESGCMEncryptionServiceTest {

    private AESGCMEncryptionService encryptionService;
    private String testSecretKey;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // GCM 표준 IV 길이
    private static final int GCM_TAG_LENGTH = 16; // GCM 태그 길이 (128 bits)

    @BeforeEach
    void setUp() throws Exception {
        // 채널 전달용 AES-256 키 생성 (32바이트 = 256비트)
        testSecretKey = generateAES256Key();
        encryptionService = new AESGCMEncryptionService();
    }

    @Test
    @DisplayName("AES-256-GCM 암호화 및 복호화 테스트")
    void testEncryptionAndDecryption() throws Exception {
        // Given
        String originalText = "홍길동";
        String personalInfo = "{\"name\":\"홍길동\",\"phone\":\"010-1234-5678\",\"ssn\":\"123456-1234567\"}";

        // When - 암호화
        String encryptedText = encryptionService.encrypt(originalText, testSecretKey);
        String encryptedPersonalInfo = encryptionService.encrypt(personalInfo, testSecretKey);

        // Then - 복호화 검증
        String decryptedText = encryptionService.decrypt(encryptedText, testSecretKey);
        String decryptedPersonalInfo = encryptionService.decrypt(encryptedPersonalInfo, testSecretKey);

        assertEquals(originalText, decryptedText);
        assertEquals(personalInfo, decryptedPersonalInfo);
        
        // 암호화된 텍스트는 원문과 달라야 함
        assertNotEquals(originalText, encryptedText);
        assertNotEquals(personalInfo, encryptedPersonalInfo);

        System.out.println("=== AES-256-GCM 암호화 테스트 결과 ===");
        System.out.println("원문: " + originalText);
        System.out.println("암호문: " + encryptedText);
        System.out.println("복호문: " + decryptedText);
        System.out.println("개인정보 암호문 길이: " + encryptedPersonalInfo.length() + " bytes");
    }

    @Test
    @DisplayName("채널 전달용 키 생성 및 사용법 테스트")
    void testKeyGenerationForChannel() throws Exception {
        // 1. 채널별 키 생성 예제
        String mobileAppKey = generateAES256Key();
        String webAppKey = generateAES256Key();
        String apiGwKey = generateAES256Key();

        System.out.println("\n=== 채널별 AES-256 키 생성 결과 ===");
        System.out.println("MOBILE_APP 키: " + mobileAppKey);
        System.out.println("WEB_APP 키: " + webAppKey);
        System.out.println("API_GW 키: " + apiGwKey);
        
        // 2. 각 키로 암호화/복호화 테스트
        String testData = "개인정보테스트데이터";
        
        String encrypted1 = encryptionService.encrypt(testData, mobileAppKey);
        String encrypted2 = encryptionService.encrypt(testData, webAppKey);
        String encrypted3 = encryptionService.encrypt(testData, apiGwKey);
        
        // 같은 평문이라도 다른 키로 암호화하면 다른 결과
        assertNotEquals(encrypted1, encrypted2);
        assertNotEquals(encrypted2, encrypted3);
        
        // 각각 복호화 성공
        assertEquals(testData, encryptionService.decrypt(encrypted1, mobileAppKey));
        assertEquals(testData, encryptionService.decrypt(encrypted2, webAppKey));
        assertEquals(testData, encryptionService.decrypt(encrypted3, apiGwKey));
    }

    @Test
    @DisplayName("잘못된 키로 복호화 시 실패 테스트")
    void testDecryptionWithWrongKey() {
        // Given
        String originalText = "테스트 데이터";
        String correctKey = testSecretKey;
        String wrongKey = generateAES256Key();

        // When
        String encrypted = encryptionService.encrypt(originalText, correctKey);

        // Then - 잘못된 키로 복호화하면 예외 발생
        assertThrows(RuntimeException.class, () -> {
            encryptionService.decrypt(encrypted, wrongKey);
        });
    }

    /**
     * 채널 전달용 AES-256 키 생성
     * @return Base64 인코딩된 256비트 키
     */
    private String generateAES256Key() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // 256비트 키 생성
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("AES-256 키 생성 실패", e);
        }
    }

    /**
     * AES-256-GCM 암호화/복호화 서비스 구현체
     */
    static class AESGCMEncryptionService {

        /**
         * AES-256-GCM으로 평문 암호화
         * 
         * @param plainText 암호화할 평문
         * @param base64Key Base64 인코딩된 AES-256 키
         * @return Base64 인코딩된 암호문 (IV + 암호문 + 태그)
         */
        public String encrypt(String plainText, String base64Key) {
            try {
                // Base64 키를 디코딩하여 SecretKey 생성
                byte[] keyBytes = Base64.getDecoder().decode(base64Key);
                SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

                // Cipher 초기화
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                
                // 랜덤 IV 생성 (12바이트)
                byte[] iv = new byte[GCM_IV_LENGTH];
                SecureRandom.getInstanceStrong().nextBytes(iv);
                
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

                // 암호화 수행
                byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
                byte[] cipherBytes = cipher.doFinal(plainBytes);

                // IV + 암호문을 하나의 바이트 배열로 결합
                byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + cipherBytes.length];
                System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
                System.arraycopy(cipherBytes, 0, encryptedWithIv, GCM_IV_LENGTH, cipherBytes.length);

                return Base64.getEncoder().encodeToString(encryptedWithIv);

            } catch (Exception e) {
                throw new RuntimeException("AES-256-GCM 암호화 실패", e);
            }
        }

        /**
         * AES-256-GCM으로 암호문 복호화
         * 
         * @param encryptedText Base64 인코딩된 암호문 (IV + 암호문 + 태그)
         * @param base64Key Base64 인코딩된 AES-256 키
         * @return 복호화된 평문
         */
        public String decrypt(String encryptedText, String base64Key) {
            try {
                // Base64 암호문 디코딩
                byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);

                // IV와 암호문 분리
                byte[] iv = new byte[GCM_IV_LENGTH];
                byte[] cipherBytes = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
                
                System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);
                System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.length);

                // Base64 키를 디코딩하여 SecretKey 생성
                byte[] keyBytes = Base64.getDecoder().decode(base64Key);
                SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

                // Cipher 초기화
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

                // 복호화 수행
                byte[] plainBytes = cipher.doFinal(cipherBytes);
                return new String(plainBytes, StandardCharsets.UTF_8);

            } catch (Exception e) {
                throw new RuntimeException("AES-256-GCM 복호화 실패", e);
            }
        }
    }
}