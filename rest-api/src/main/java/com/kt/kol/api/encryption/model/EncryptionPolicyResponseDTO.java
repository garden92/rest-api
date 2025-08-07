package com.kt.kol.api.encryption.model;

import java.util.List;

public record EncryptionPolicyResponseDTO(
    String clientId,
    String apiPath,
    String httpMethod,
    List<EncryptionFieldInfoDTO> encryptionFields,
    EncryptionKeyInfoDTO keyInfo
) {
}