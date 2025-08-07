package com.kt.kol.api.encryption.model;

public record EncryptionKeyDTO(
    String keyId,
    String clientId,
    String algorithm,
    byte[] keyMaterial,
    Integer keyVersion,
    String status
) {
}