package com.kt.kol.api.encryption.model;

public record EncryptionFieldInfoDTO(
    String fieldName,
    String direction,
    String algorithm
) {
}