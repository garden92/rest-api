package com.kt.kol.api.encryption.model;

public record EncryptionKeyInfoDTO(
    String keyId,
    String algorithm,
    Integer keyVersion,
    String keyMaterial // Base64 인코딩된 키 정보
) {
}