package com.kt.kol.api.encryption.service;

import java.util.Base64;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kt.kol.api.encryption.model.EncryptionFieldInfoDTO;
import com.kt.kol.api.encryption.model.EncryptionKeyInfoDTO;
import com.kt.kol.api.encryption.model.EncryptionPolicyDTO;
import com.kt.kol.api.encryption.model.EncryptionPolicyResponseDTO;
import com.kt.kol.api.encryption.model.EncryptionRequestDTO;
import com.kt.kol.api.encryption.repository.EncryptionKeyRepository;
import com.kt.kol.api.encryption.repository.EncryptionPolicyRepository;
import com.kt.kol.common.model.ResponseStdVO;
import com.kt.kol.common.model.TrtErrInfoDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class EncryptionPolicyService {

    private final EncryptionPolicyRepository encryptionPolicyRepository;
    private final EncryptionKeyRepository encryptionKeyRepository;

    /**
     * 특정 채널과 API 경로에 대한 암호화 정책 및 키 정보 조회
     * 
     * @param requestDTO 암호화 정책 요청 정보
     * @return 암호화 필드 목록과 키 정보
     */
    public Mono<ResponseStdVO<EncryptionPolicyResponseDTO>> getEncryptionPolicy(EncryptionRequestDTO requestDTO) {
        
        log.debug("암호화 정책 조회 시작. clientId=[{}], apiPath=[{}], method=[{}]", 
                requestDTO.clientId(), requestDTO.apiPath(), requestDTO.httpMethod());

        // 암호화 정책과 키 정보를 병렬로 조회
        Mono<java.util.List<EncryptionPolicyDTO>> policyListMono = encryptionPolicyRepository
                .findEncryptionPolicyByClientAndApi(
                    requestDTO.clientId(), 
                    requestDTO.apiPath(), 
                    requestDTO.httpMethod())
                .collectList();

        Mono<com.kt.kol.api.encryption.model.EncryptionKeyDTO> keyMono = encryptionKeyRepository
                .findActiveKeyByClient(requestDTO.clientId())
                .switchIfEmpty(Mono.error(new RuntimeException("암호화 키를 찾을 수 없습니다.")));

        return Mono.zip(policyListMono, keyMono)
                .flatMap(tuple -> {
                    var policyList = tuple.getT1();
                    var keyDto = tuple.getT2();

                    // 정책이 없는 경우 빈 응답 반환
                    if (policyList.isEmpty()) {
                        log.debug("암호화 정책이 없습니다. clientId=[{}], apiPath=[{}]", 
                                requestDTO.clientId(), requestDTO.apiPath());
                        
                        TrtErrInfoDTO successInfo = new TrtErrInfoDTO("I", "", "", "");
                        EncryptionPolicyResponseDTO emptyResponse = new EncryptionPolicyResponseDTO(
                            requestDTO.clientId(),
                            requestDTO.apiPath(),
                            requestDTO.httpMethod(),
                            java.util.Collections.emptyList(),
                            null
                        );
                        return Mono.just(new ResponseStdVO<>(successInfo, emptyResponse));
                    }

                    // 암호화 필드 정보 변환
                    var encryptionFields = policyList.stream()
                            .map(policy -> new EncryptionFieldInfoDTO(
                                policy.fieldName(),
                                policy.direction(),
                                keyDto.algorithm()
                            ))
                            .collect(Collectors.toList());

                    // 키 정보 변환 (보안상 실제 키 값은 Base64 인코딩)
                    EncryptionKeyInfoDTO keyInfo = new EncryptionKeyInfoDTO(
                        keyDto.keyId(),
                        keyDto.algorithm(),
                        keyDto.keyVersion(),
                        Base64.getEncoder().encodeToString(keyDto.keyMaterial())
                    );

                    // 응답 DTO 생성
                    EncryptionPolicyResponseDTO responseData = new EncryptionPolicyResponseDTO(
                        requestDTO.clientId(),
                        requestDTO.apiPath(),
                        requestDTO.httpMethod(),
                        encryptionFields,
                        keyInfo
                    );

                    TrtErrInfoDTO successInfo = new TrtErrInfoDTO("I", "", "", "");
                    
                    log.debug("암호화 정책 조회 완료. 필드 수=[{}], keyId=[{}]", 
                            encryptionFields.size(), keyInfo.keyId());

                    return Mono.just(new ResponseStdVO<>(successInfo, responseData));
                })
                .onErrorResume(error -> {
                    log.error("암호화 정책 조회 중 오류 발생", error);
                    
                    TrtErrInfoDTO errorInfo = new TrtErrInfoDTO("E", "KOLE1001", 
                            "암호화 정책 조회 실패: " + error.getMessage(), "");
                    
                    return Mono.just(new ResponseStdVO<>(errorInfo, null));
                });
    }

    /**
     * 특정 채널의 모든 암호화 정책 조회 (관리용)
     * 
     * @param clientId 클라이언트 ID
     * @return 해당 채널의 모든 암호화 정책 목록
     */
    public Mono<ResponseStdVO<java.util.List<EncryptionPolicyDTO>>> getAllEncryptionPoliciesByClient(String clientId) {
        
        log.debug("채널별 전체 암호화 정책 조회 시작. clientId=[{}]", clientId);

        return encryptionPolicyRepository.findEncryptionPolicyByClient(clientId)
                .collectList()
                .map(policyList -> {
                    TrtErrInfoDTO successInfo = new TrtErrInfoDTO("I", "", "", "");
                    
                    log.debug("채널별 전체 암호화 정책 조회 완료. 정책 수=[{}]", policyList.size());
                    
                    return new ResponseStdVO<>(successInfo, policyList);
                })
                .onErrorResume(error -> {
                    log.error("채널별 암호화 정책 조회 중 오류 발생", error);
                    
                    TrtErrInfoDTO errorInfo = new TrtErrInfoDTO("E", "KOLE1002", 
                            "채널별 암호화 정책 조회 실패: " + error.getMessage(), "");
                    
                    return Mono.just(new ResponseStdVO<>(errorInfo, null));
                });
    }
}