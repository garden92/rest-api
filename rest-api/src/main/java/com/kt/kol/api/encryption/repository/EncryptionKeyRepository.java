package com.kt.kol.api.encryption.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.kt.kol.api.encryption.model.EncryptionKeyDTO;

import reactor.core.publisher.Mono;

@Repository
public interface EncryptionKeyRepository extends ReactiveCrudRepository<EncryptionKeyDTO, String> {

    @Query("""
        SELECT key_id,
               client_id,
               algorithm,
               key_material,
               key_version,
               status
        FROM kolown.kol_encryption_key
        WHERE client_id = :clientId
          AND status = 'A'
        ORDER BY key_version DESC
        LIMIT 1
        """)
    Mono<EncryptionKeyDTO> findActiveKeyByClient(String clientId);

    @Query("""
        SELECT key_id,
               client_id,
               algorithm,
               key_material,
               key_version,
               status
        FROM kolown.kol_encryption_key
        WHERE key_id = :keyId
          AND status = 'A'
        """)
    Mono<EncryptionKeyDTO> findActiveKeyById(String keyId);
}