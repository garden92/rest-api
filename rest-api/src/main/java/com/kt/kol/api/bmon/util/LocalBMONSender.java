package com.kt.kol.api.bmon.util;

import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import com.kt.kol.common.model.TrtErrInfoDTO;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 로컬 환경 전용 BMON 송신기
 * 실제 BMON 연동 없이 입력 로그만 출력하고 종료
 */
@Service
@Slf4j
@Profile("local")
public class LocalBMONSender implements BmonSenderInterface {

    /**
     * 로컬 환경 BMON 연동 처리 (실제 연동 없이 로그만 출력)
     * 
     * @param TrFlag        거래 플래그 (T/R)
     * @param inDTO         입력 DTO
     * @param trtErrInfoDTO 거래 오류 정보
     * @param request       HTTP 요청 객체
     * @return Mono<Void>
     */
    @Override
    public <T> Mono<Void> sendBmonMot(String TrFlag, T inDTO, TrtErrInfoDTO trtErrInfoDTO, ServerHttpRequest request) {

        return Mono.fromRunnable(() -> {
            log.info("===== 로컬 BMON 송신기 =====");
            log.info("거래 플래그: {}", TrFlag);
            log.info("요청 URI: {}", request.getURI());
            log.info("요청 메소드: {}", request.getMethod());
            log.info("응답 타입: {}", trtErrInfoDTO != null ? trtErrInfoDTO.responseType() : "null");
            log.info("응답 코드: {}", trtErrInfoDTO != null ? trtErrInfoDTO.responseCode() : "null");

            if (inDTO != null) {
                log.info("입력 DTO 클래스: {}", inDTO.getClass().getSimpleName());
                log.info("입력 DTO 내용: {}", inDTO.toString());
            } else {
                log.info("입력 DTO: null");
            }

            log.info("로컬 환경이므로 실제 BMON 연동은 수행하지 않습니다.");
            log.info("===========================");
        }).then();
    }
}