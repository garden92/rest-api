package com.kt.kol.api.encryption.model;

public record EncryptionPolicyDTO(
    String policyId,
    String clientId,
    String apiPath,
    String httpMethod,
    String fieldName,
    String direction,
    String status
) {
}