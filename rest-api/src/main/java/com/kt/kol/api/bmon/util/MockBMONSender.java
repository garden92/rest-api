package com.kt.kol.api.bmon.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import com.kt.kol.common.model.TrtErrInfoDTO;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Mock 환경 전용 BMON 송신기
 * 실제 BMON 연동을 시뮬레이션하며, 지연시간과 실패율을 설정 가능
 */
@Service
@Slf4j
@Profile("mock")
public class MockBMONSender implements BmonSenderInterface {

    @Value("${mock.delay.min:100}")
    private int minDelay;

    @Value("${mock.delay.max:500}")
    private int maxDelay;

    @Value("${mock.failure.rate:0.1}")
    private double failureRate;

    private final Random random = new Random();

    /**
     * Mock 환경 BMON 연동 처리 (시뮬레이션)
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
            log.info("===== Mock BMON 송신기 =====");
            log.info("거래 플래그: {}", TrFlag);
            log.info("요청 URI: {}", request.getURI());
            log.info("요청 메소드: {}", request.getMethod());
            log.info("응답 타입: {}", trtErrInfoDTO != null ? trtErrInfoDTO.responseType() : "null");
            log.info("응답 코드: {}", trtErrInfoDTO != null ? trtErrInfoDTO.responseCode() : "null");

            if (inDTO != null) {
                log.info("입력 DTO 클래스: {}", inDTO.getClass().getSimpleName());
                log.debug("입력 DTO 내용: {}", inDTO.toString());
            } else {
                log.info("입력 DTO: null");
            }

            // 실패 시뮬레이션 (예외 던지지 않음)
            if (random.nextDouble() < failureRate) {
                log.warn("Mock BMON 전송 실패 시뮬레이션 (실패율: {}%, 실제 동작에는 영향 없음)", failureRate * 100);
            } else {
                log.info("Mock BMON 전송 성공 시뮬레이션");
            }

            log.info("Mock BMON 전송 완료 시뮬레이션");
            log.info("============================");
        })
                .delayElement(java.time.Duration.ofMillis(
                        ThreadLocalRandom.current().nextInt(minDelay, maxDelay + 1)))
                .then()
                .doOnError(e -> log.error("Mock BMON 전송 중 오류 발생: {}", e.getMessage()));
    }
}