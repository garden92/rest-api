package com.kt.kol.api.encryption.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kt.kol.api.encryption.model.EcodPlcyHstDTO;
import com.kt.kol.api.encryption.repository.EcodPlcyHstRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 암호화 정책 이력 서비스
 * 키는 별도 KeyVault에서 관리하고, 정책만 DB에서 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EcodPlcyHstService {

    private final EcodPlcyHstRepository ecodPlcyHstRepository;

    /**
     * 현재 유효한 암호화 정책 조회
     * @param chId 채널 ID
     * @param fieldNm 필드명
     * @return 현재 유효한 정책
     */
    public Mono<EcodPlcyHstDTO> getCurrentValidPolicy(String chId, String fieldNm) {
        log.debug("현재 유효한 암호화 정책 조회 - 채널: {}, 필드: {}", chId, fieldNm);
        
        LocalDateTime now = LocalDateTime.now();
        return ecodPlcyHstRepository.findCurrentValidPolicy(chId, fieldNm, now)
                .doOnNext(policy -> log.debug("유효한 정책 발견: {}", policy));
    }

    /**
     * 특정 시점의 유효한 암호화 정책 조회
     * @param chId 채널 ID
     * @param fieldNm 필드명
     * @param targetTime 확인할 시점
     * @return 해당 시점의 유효한 정책
     */
    public Mono<EcodPlcyHstDTO> getValidPolicyAt(String chId, String fieldNm, LocalDateTime targetTime) {
        log.debug("특정 시점 유효한 암호화 정책 조회 - 채널: {}, 필드: {}, 시점: {}", chId, fieldNm, targetTime);
        
        return ecodPlcyHstRepository.findCurrentValidPolicy(chId, fieldNm, targetTime)
                .doOnNext(policy -> log.debug("해당 시점 유효한 정책 발견: {}", policy));
    }

    /**
     * 채널의 모든 현재 유효한 정책 조회
     * @param chId 채널 ID
     * @return 현재 유효한 모든 정책 목록
     */
    public Flux<EcodPlcyHstDTO> getAllCurrentValidPoliciesByChannel(String chId) {
        log.debug("채널의 모든 현재 유효한 정책 조회 - 채널: {}", chId);
        
        LocalDateTime now = LocalDateTime.now();
        return ecodPlcyHstRepository.findAllCurrentValidPoliciesByChannel(chId, now)
                .doOnNext(policy -> log.debug("유효한 정책: {}", policy.getFieldNm()))
                .doOnComplete(() -> log.debug("채널 {} 정책 조회 완료", chId));
    }

    /**
     * 모든 현재 유효한 정책 조회
     * @return 현재 유효한 모든 정책 목록
     */
    public Flux<EcodPlcyHstDTO> getAllCurrentValidPolicies() {
        log.debug("모든 현재 유효한 정책 조회");
        
        LocalDateTime now = LocalDateTime.now();
        return ecodPlcyHstRepository.findAllCurrentValidPolicies(now)
                .doOnNext(policy -> log.debug("유효한 정책: {} - {}", policy.getChId(), policy.getFieldNm()))
                .doOnComplete(() -> log.debug("전체 정책 조회 완료"));
    }

    /**
     * 정책 이력 조회
     * @param chId 채널 ID
     * @param fieldNm 필드명
     * @return 해당 필드의 모든 정책 이력
     */
    public Flux<EcodPlcyHstDTO> getPolicyHistory(String chId, String fieldNm) {
        log.debug("정책 이력 조회 - 채널: {}, 필드: {}", chId, fieldNm);
        
        return ecodPlcyHstRepository.findPolicyHistory(chId, fieldNm)
                .doOnNext(policy -> log.debug("정책 이력: 유효기간 {} ~ {}", policy.getEfctStDt(), policy.getEfctFnsDt()))
                .doOnComplete(() -> log.debug("정책 이력 조회 완료"));
    }

    /**
     * 필드가 암호화 대상인지 확인
     * @param chId 채널 ID
     * @param fieldNm 필드명
     * @return 암호화 대상이면 true
     */
    public Mono<Boolean> isEncryptionRequired(String chId, String fieldNm) {
        return getCurrentValidPolicy(chId, fieldNm)
                .map(policy -> true)
                .defaultIfEmpty(false)
                .doOnNext(required -> log.debug("암호화 필요 여부 - 채널: {}, 필드: {}, 필요: {}", chId, fieldNm, required));
    }

    /**
     * 채널의 암호화 필드 목록 조회
     * @param chId 채널 ID
     * @return 암호화가 필요한 필드명 목록
     */
    public Flux<String> getEncryptionFieldNames(String chId) {
        return getAllCurrentValidPoliciesByChannel(chId)
                .map(EcodPlcyHstDTO::getFieldNm)
                .distinct()
                .doOnNext(fieldNm -> log.debug("암호화 필드: {}", fieldNm));
    }
}