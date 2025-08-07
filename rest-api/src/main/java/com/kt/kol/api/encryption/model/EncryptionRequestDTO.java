package com.kt.kol.api.encryption.model;

public record EncryptionRequestDTO(
    String clientId,
    String apiPath,
    String httpMethod
) {
}