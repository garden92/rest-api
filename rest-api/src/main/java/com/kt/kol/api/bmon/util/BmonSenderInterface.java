package com.kt.kol.api.bmon.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

import com.kt.kol.common.model.TrtErrInfoDTO;

import reactor.core.publisher.Mono;

/**
 * BMON 송신기 공통 인터페이스
 */
public interface BmonSenderInterface {
    
    /**
     * BMON 연동 처리
     * 
     * @param TrFlag 거래 플래그 (T/R)
     * @param inDTO 입력 DTO
     * @param trtErrInfoDTO 거래 오류 정보
     * @param request HTTP 요청 객체
     * @return Mono<Void>
     */
    <T> Mono<Void> sendBmonMot(String TrFlag, T inDTO, TrtErrInfoDTO trtErrInfoDTO, ServerHttpRequest request);
}