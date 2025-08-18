package com.kt.kol.api.mock.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Mock 외부 서비스
 * 다양한 외부 시스템 연동을 시뮬레이션
 */
@Service
@Profile("mock")
@Slf4j
public class MockExternalService {

    @Value("${mock.delay.min:100}")
    private int minDelay;
    
    @Value("${mock.delay.max:500}")
    private int maxDelay;
    
    @Value("${mock.failure.rate:0.1}")
    private double failureRate;

    /**
     * Mock 사용자 정보 조회
     */
    public Mono<Map<String, Object>> getUserInfo(String userId) {
        return simulateDelay()
            .then(simulateFailure())
            .then(Mono.fromCallable(() -> {
                log.info("Mock 사용자 정보 조회: userId={}", userId);
                
                return Map.of(
                    "userId", userId,
                    "userName", "Mock User " + userId,
                    "email", userId + "@mock.com",
                    "department", "Mock Department",
                    "role", "USER",
                    "lastLoginAt", System.currentTimeMillis() - 3600000,
                    "isActive", true
                );
            }));
    }

    /**
     * Mock 권한 확인
     */
    public Mono<Boolean> checkPermission(String userId, String resource) {
        return simulateDelay()
            .then(simulateFailure())
            .then(Mono.fromCallable(() -> {
                log.info("Mock 권한 확인: userId={}, resource={}", userId, resource);
                
                // Mock 로직: admin_user는 모든 권한, test_user는 제한적 권한
                if ("admin_user".equals(userId)) {
                    return true;
                } else if ("test_user".equals(userId)) {
                    return resource.contains("read") || resource.contains("view");
                }
                
                return false;
            }));
    }

    /**
     * Mock 로그 전송
     */
    public Mono<String> sendLog(Map<String, Object> logData) {
        return simulateDelay()
            .then(simulateFailure())
            .then(Mono.fromCallable(() -> {
                log.info("Mock 로그 전송: {}", logData);
                
                String logId = "LOG-" + System.currentTimeMillis();
                log.debug("Mock 로그 전송 완료: logId={}", logId);
                
                return logId;
            }));
    }

    /**
     * Mock 배치 데이터 처리
     */
    public Flux<Map<String, Object>> processBatchData(List<Map<String, Object>> batchData) {
        return Flux.fromIterable(batchData)
            .delayElements(Duration.ofMillis(minDelay))
            .flatMap(data -> {
                return simulateFailure()
                    .then(Mono.fromCallable(() -> {
                        log.debug("Mock 배치 데이터 처리: {}", data);
                        
                        return Map.of(
                            "originalData", data,
                            "processedAt", System.currentTimeMillis(),
                            "status", "PROCESSED",
                            "processId", "BATCH-" + System.currentTimeMillis()
                        );
                    }));
            });
    }

    /**
     * Mock 통계 데이터 조회
     */
    public Mono<Map<String, Object>> getStatistics(String type, String period) {
        return simulateDelay()
            .then(simulateFailure())
            .then(Mono.fromCallable(() -> {
                log.info("Mock 통계 데이터 조회: type={}, period={}", type, period);
                
                return Map.of(
                    "type", type,
                    "period", period,
                    "totalCount", ThreadLocalRandom.current().nextInt(100, 1000),
                    "successCount", ThreadLocalRandom.current().nextInt(80, 95),
                    "failureCount", ThreadLocalRandom.current().nextInt(5, 20),
                    "averageResponseTime", ThreadLocalRandom.current().nextInt(100, 500),
                    "generatedAt", System.currentTimeMillis()
                );
            }));
    }

    /**
     * Mock 알림 전송
     */
    public Mono<Boolean> sendNotification(String recipient, String message, String type) {
        return simulateDelay()
            .then(simulateFailure())
            .then(Mono.fromCallable(() -> {
                log.info("Mock 알림 전송: recipient={}, type={}, message={}", recipient, type, message);
                
                // Mock 로직: 특정 조건에서 전송 실패
                if (message.contains("ERROR") || message.contains("CRITICAL")) {
                    return ThreadLocalRandom.current().nextDouble() > 0.1; // 10% 실패
                }
                
                return true;
            }));
    }

    /**
     * 지연 시간 시뮬레이션
     */
    private Mono<Void> simulateDelay() {
        int delay = ThreadLocalRandom.current().nextInt(minDelay, maxDelay + 1);
        return Mono.delay(Duration.ofMillis(delay)).then();
    }

    /**
     * 실패 시뮬레이션
     */
    private Mono<Void> simulateFailure() {
        return Mono.fromRunnable(() -> {
            if (ThreadLocalRandom.current().nextDouble() < failureRate) {
                log.warn("Mock 외부 서비스 실패 시뮬레이션 (실패율: {}%)", failureRate * 100);
                throw new RuntimeException("Mock 외부 서비스 일시적 장애");
            }
        });
    }
}