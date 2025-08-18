package com.kt.kol.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Mock 환경에서 테스트 데이터를 자동으로 초기화하는 컴포넌트
 */
@Component
@Profile("mock")
@RequiredArgsConstructor
@Slf4j
public class MockDataInitializer implements ApplicationRunner {

    private final DatabaseClient databaseClient;
    
    @Value("${mock.database.auto-populate:true}")
    private boolean autoPopulate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!autoPopulate) {
            log.info("Mock 데이터 자동 생성이 비활성화되어 있습니다.");
            return;
        }
        
        log.info("Mock 환경 데이터베이스 초기화를 시작합니다...");
        
        try {
            // 스키마 생성
            executeScript("db/h2/schema.sql")
                .then(executeScript("db/h2/data.sql"))
                .doOnSuccess(v -> log.info("Mock 데이터베이스 초기화가 완료되었습니다."))
                .doOnError(e -> log.error("Mock 데이터베이스 초기화 중 오류 발생: {}", e.getMessage(), e))
                .subscribe();
                
        } catch (Exception e) {
            log.error("Mock 데이터 초기화 실패: {}", e.getMessage(), e);
        }
    }

    private Mono<Void> executeScript(String scriptPath) {
        return Mono.fromCallable(() -> {
            try {
                Resource resource = new ClassPathResource(scriptPath);
                String script = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
                
                // SQL 스크립트를 세미콜론으로 분리하여 실행
                String[] statements = script.split(";");
                
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        databaseClient.sql(trimmed)
                            .fetch()
                            .rowsUpdated()
                            .subscribe(
                                count -> log.debug("SQL 실행 완료: {} rows affected", count),
                                error -> log.warn("SQL 실행 경고: {}", error.getMessage())
                            );
                    }
                }
                
                log.info("스크립트 실행 완료: {}", scriptPath);
                return null;
            } catch (Exception e) {
                throw new RuntimeException("스크립트 실행 실패: " + scriptPath, e);
            }
        }).then();
    }
}