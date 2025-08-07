package com.kt.kol.api.encryption.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.kt.kol.api.encryption.model.EncryptionPolicyDTO;

import reactor.core.publisher.Flux;

@Repository
public interface EncryptionPolicyRepository extends ReactiveCrudRepository<EncryptionPolicyDTO, String> {

    @Query("""
        SELECT policy_id,
               client_id,
               api_path,
               http_method,
               field_name,
               direction,
               status
        FROM kolown.kol_encryption_policy
        WHERE client_id = :clientId
          AND api_path = :apiPath
          AND http_method = :httpMethod
          AND status = 'A'
        ORDER BY field_name
        """)
    Flux<EncryptionPolicyDTO> findEncryptionPolicyByClientAndApi(String clientId, String apiPath, String httpMethod);

    @Query("""
        SELECT policy_id,
               client_id,
               api_path,
               http_method,
               field_name,
               direction,
               status
        FROM kolown.kol_encryption_policy
        WHERE client_id = :clientId
          AND status = 'A'
        ORDER BY api_path, field_name
        """)
    Flux<EncryptionPolicyDTO> findEncryptionPolicyByClient(String clientId);
}