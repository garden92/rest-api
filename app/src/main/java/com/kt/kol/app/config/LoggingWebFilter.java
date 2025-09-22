package com.kt.kol.app.config;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.WebFilter;

import lombok.extern.slf4j.Slf4j;
import reactor.util.context.Context;

@Configuration
@Slf4j
public class LoggingWebFilter {

    @Bean
    public WebFilter requestResponseLoggingFilter() {
        return (exchange, chain) -> {
            if (shouldSkipLogging(exchange.getRequest())) {
                return chain.filter(exchange);
            }

            String correlationId = UUID.randomUUID().toString().substring(0, 8);
            Instant startTime = Instant.now();

            ServerHttpRequest request = exchange.getRequest();

            // MDC에 correlation ID 설정
            return chain.filter(exchange)
                    .contextWrite(Context.of("correlationId", correlationId))
                    .doOnSubscribe(subscription -> {
                        MDC.put("correlationId", correlationId);
                        logRequest(request, correlationId);
                    })
                    .doFinally(signalType -> {
                        Instant endTime = Instant.now();
                        Duration duration = Duration.between(startTime, endTime);
                        ServerHttpResponse response = exchange.getResponse();
                        logResponse(response, correlationId, duration);
                        MDC.clear();
                    });
        };
    }

    private void logRequest(ServerHttpRequest request, String correlationId) {
        log.info("[{}] --> {} {} | Remote: {} | User-Agent: {}",
                correlationId,
                request.getMethod(),
                request.getURI(),
                getClientIp(request),
                request.getHeaders().getFirst("User-Agent"));
    }

    private void logResponse(ServerHttpResponse response, String correlationId, Duration duration) {
        log.info("[{}] <-- {} | Duration: {}ms",
                correlationId,
                response.getStatusCode(),
                duration.toMillis());
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    private boolean shouldSkipLogging(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        return path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".ico");
    }
}