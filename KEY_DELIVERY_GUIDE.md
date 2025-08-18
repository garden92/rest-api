# 채널 암호화 키 전달 가이드

## 1. 키 전달 방식 개요

채널에 AES-256-GCM 암호화 키를 안전하게 전달하는 방법과 사용법을 설명합니다.

### 보안 원칙

- **절대 평문으로 키를 전달하지 않음**
- **안전한 채널을 통한 전달** (HTTPS, VPN, 암호화된 메시지)
- **키 버전 관리** 및 주기적 키 교체

## 2. 키 전달 방법

### 방법 1: HTTPS API를 통한 키 배포 (권장)

```bash
# API 호출 예제 (채널에서 키 요청)
curl -X POST https://kol-api.kt.com/restGw/encryption/policy \
  -H "Content-Type: application/json" \
  -H "X-API-Key: [채널인증키]" \
  -d '{
    "clientId": "AI"
  }'
```

**응답 예시:**

```json
{
  "trtErrInfoDTO": {
    "trtErrTyp": "I"
  },
  "data": {
    "clientId": "AI",
    "encryptionFields": [
      {
        "fieldName": "password"
      },
      {
        "fieldName": "personalInfo"
      }
    ],
    "keyInfo": {
      "keyId": "KEY_AI_V1",
      "algorithm": "AES-256-GCM",
      "keyVersion": 1,
      "keyMaterial": "YWJjZGVmZ2hpams789abcdefghijklmnopqrstuvwxyz123456789ABCDEF="
    }
  }
}
```

### 방법 2: 안전한 파일 전송

```json
{
  "keyId": "KEY_AI_V1",
  "algorithm": "AES-256-GCM",
  "keyVersion": 1,
  "keyMaterial": "YWJjZGVmZ2hpams789abcdefghijklmnopqrstuvwxyz123456789ABCDEF=",
  "createdAt": "2024-01-15 10:30:00",
  "usage": "FIELD_ENCRYPTION",
  "instructions": {
    "algorithm": "AES-256-GCM",
    "keySize": "256 bits",
    "ivLength": "12 bytes (96 bits)",
    "tagLength": "16 bytes (128 bits)",
    "encoding": "Base64"
  }
}
```

### 방법 3: RSA 키 교환 방식 (최고 보안)

1. **서버에서 RSA 키페어 생성**
2. **채널에 RSA 공개키 전달**
3. **채널에서 AES 키를 RSA 공개키로 암호화하여 전송**
4. **서버에서 RSA 개인키로 복호화**

## 3. 채널별 키 사용 예제

### Java 구현 예제 (AI/KI 채널용)

```java
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESGCMUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    /**
     * 필드 암호화
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
     * 필드 복호화
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
```

### JavaScript (Node.js) 구현 예제 (AI/KI 채널용)

```javascript
const crypto = require("crypto");

class AESGCMUtil {
  static ALGORITHM = "aes-256-gcm";
  static IV_LENGTH = 12;

  /**
   * 필드 암호화
   */
  static encryptField(plainText, base64Key) {
    try {
      // Base64 키 디코딩
      const key = Buffer.from(base64Key, "base64");

      // 랜덤 IV 생성
      const iv = crypto.randomBytes(this.IV_LENGTH);

      // 암호화
      const cipher = crypto.createCipher(this.ALGORITHM, key);
      cipher.setAutoPadding(false);

      let encrypted = cipher.update(plainText, "utf8");
      encrypted = Buffer.concat([encrypted, cipher.final()]);

      // 인증 태그 획득
      const tag = cipher.getAuthTag();

      // IV + 암호문 + 태그 결합
      const result = Buffer.concat([iv, encrypted, tag]);

      return result.toString("base64");
    } catch (error) {
      throw new Error("암호화 실패: " + error.message);
    }
  }

  /**
   * 필드 복호화
   */
  static decryptField(encryptedText, base64Key) {
    try {
      // Base64 암호문 디코딩
      const encryptedBuffer = Buffer.from(encryptedText, "base64");

      // IV, 암호문, 태그 분리
      const iv = encryptedBuffer.slice(0, this.IV_LENGTH);
      const tag = encryptedBuffer.slice(-16);
      const encrypted = encryptedBuffer.slice(this.IV_LENGTH, -16);

      // Base64 키 디코딩
      const key = Buffer.from(base64Key, "base64");

      // 복호화
      const decipher = crypto.createDecipher(this.ALGORITHM, key);
      decipher.setAuthTag(tag);

      let decrypted = decipher.update(encrypted, null, "utf8");
      decrypted += decipher.final("utf8");

      return decrypted;
    } catch (error) {
      throw new Error("복호화 실패: " + error.message);
    }
  }
}

module.exports = AESGCMUtil;
```

## 4. 실제 사용 시나리오

### 로그인 API 예제

**요청 데이터 (채널 → 서버):**

```json
{
  "userId": "user123",
  "password": "K7LxvQd8+Fh2j9ZmNQp6rA==", // 암호화된 패스워드
  "deviceInfo": {
    "deviceId": "dev456",
    "model": "iPhone 13"
  }
}
```

**응답 데이터 (서버 → 채널):**

```json
{
  "result": "SUCCESS",
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "userInfo": {
    "name": "M8LxvQd8+Fh2j9ZmNQp6rA==", // 암호화된 이름
    "phone": "N9MxvQd8+Fh2j9ZmNQp6rA==", // 암호화된 전화번호
    "email": "user@example.com" // 비암호화 필드
  }
}
```

## 5. 보안 주의사항

### 키 관리 원칙

1. **키는 별도 보안 저장소에 보관** (AWS KMS, Azure Key Vault 등)
2. **키 버전 관리** - 주기적 키 교체 (예: 3개월마다)
3. **키 접근 로그 모니터링**
4. **개발/운영 환경 키 분리**

### 구현 시 주의사항

1. **IV(초기화 벡터) 재사용 금지** - 매번 랜덤 생성
2. **GCM 태그 검증** 필수 - 데이터 무결성 확인
3. **키 하드코딩 금지** - 환경변수 또는 설정 파일 사용
4. **예외 처리** - 암호화/복호화 실패 시 적절한 오류 처리

## 6. 테스트 방법

```java
@Test
void encryptionDecryptionTest() {
    // Given
    String originalPassword = "mySecretPassword123!";
    String base64Key = "YWJjZGVmZ2hpams789abcdefghijklmnopqrstuvwxyz123456789ABCDEF=";

    // When
    String encrypted = AESGCMUtil.encryptField(originalPassword, base64Key);
    String decrypted = AESGCMUtil.decryptField(encrypted, base64Key);

    // Then
    assertEquals(originalPassword, decrypted);
    assertNotEquals(originalPassword, encrypted);

    System.out.println("원문: " + originalPassword);
    System.out.println("암호문: " + encrypted);
    System.out.println("복호문: " + decrypted);
}
```

이 가이드를 참조하여 채널에서 안전하게 암호화 키를 받아 필드 암호화/복호화를 구현할 수 있습니다.
