package com.kt.kol.api.mock.controller;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Mock Gateway Integration Controller
 * 외부 Gateway 시스템을 시뮬레이션하는 Mock 컨트롤러
 */
@RestController
@RequestMapping("/mock-gw")
@Profile("mock")
@Slf4j
public class MockGatewayController {

    @Value("${mock.delay.min:100}")
    private int minDelay;
    
    @Value("${mock.delay.max:500}")
    private int maxDelay;
    
    @Value("${mock.failure.rate:0.1}")
    private double failureRate;

    /**
     * Mock Gateway Health Check
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return simulateDelay()
            .then(simulateFailure())
            .then(Mono.fromCallable(() -> {
                log.info("Mock Gateway Health Check 요청 처리");
                return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "timestamp", System.currentTimeMillis(),
                    "service", "Mock Gateway",
                    "version", "1.0.0-mock"
                ));
            }));
    }

    /**
     * Mock Gateway Route Processing
     */
    @PostMapping("/process")
    public Mono<ResponseEntity<Map<String, Object>>> processRoute(
            @RequestBody Map<String, Object> request,
            @RequestParam(required = false) String channelId) {
        
        return simulateDelay()
            .then(simulateFailure())
            .then(Mono.fromCallable(() -> {
                log.info("Mock Gateway Route 처리 요청: channelId={}, request={}", channelId, request);
                
                return ResponseEntity.ok(Map.of(
                    "resultCode", "0000",
                    "resultMessage", "성공",
                    "processedAt", System.currentTimeMillis(),
                    "channelId", channelId != null ? channelId : "UNKNOWN",
                    "data", Map.of(
                        "processId", "MOCK-" + System.currentTimeMillis(),
                        "status", "PROCESSED",
                        "originalRequest", request
                    )
                ));
            }));
    }

    /**
     * Mock Gateway Authentication
     */
    @PostMapping("/auth")
    public Mono<ResponseEntity<Map<String, Object>>> authenticate(
            @RequestBody Map<String, Object> authRequest) {
        
        return simulateDelay()
            .then(simulateFailure())
            .then(Mono.fromCallable(() -> {
                log.info("Mock Gateway 인증 요청: {}", authRequest);
                
                String userId = (String) authRequest.get("userId");
                String apiKey = (String) authRequest.get("apiKey");
                
                boolean isValid = userId != null && !userId.isEmpty() && 
                                 apiKey != null && !apiKey.isEmpty();
                
                if (isValid) {
                    return ResponseEntity.ok(Map.of(
                        "resultCode", "0000",
                        "resultMessage", "인증 성공",
                        "token", "mock-jwt-token-" + System.currentTimeMillis(),
                        "expiresIn", 3600,
                        "userId", userId
                    ));
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                            "resultCode", "4001",
                            "resultMessage", "인증 실패",
                            "error", "Invalid credentials"
                        ));
                }
            }));
    }

    /**
     * Mock Gateway Data Query
     */
    @GetMapping("/data")
    public Mono<ResponseEntity<Map<String, Object>>> queryData(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        return simulateDelay()
            .then(simulateFailure())
            .then(Mono.fromCallable(() -> {
                log.info("Mock Gateway 데이터 조회: query={}, limit={}", query, limit);
                
                // Mock 데이터 생성
                var mockData = java.util.stream.IntStream.range(1, limit + 1)
                    .mapToObj(i -> Map.of(
                        "id", i,
                        "name", "Mock Data " + i,
                        "query", query,
                        "timestamp", System.currentTimeMillis() - (i * 1000)
                    ))
                    .toList();
                
                return ResponseEntity.ok(Map.of(
                    "resultCode", "0000",
                    "resultMessage", "조회 성공",
                    "totalCount", mockData.size(),
                    "data", mockData
                ));
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
                log.warn("Mock Gateway 실패 시뮬레이션 (실패율: {}%)", failureRate * 100);
                throw new RuntimeException("Mock Gateway 서비스 일시적 장애");
            }
        });
    }
}