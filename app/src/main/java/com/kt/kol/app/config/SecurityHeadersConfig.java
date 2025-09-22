package com.kt.kol.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SecurityHeadersConfig {

    @Bean
    public WebFilter securityHeadersFilter() {
        return (exchange, chain) -> {
            var response = exchange.getResponse();
            var headers = response.getHeaders();

            // Security Headers
            headers.add("X-Content-Type-Options", "nosniff");
            headers.add("X-Frame-Options", "DENY");
            headers.add("X-XSS-Protection", "1; mode=block");
            headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
            headers.add("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'");

            // CORS Headers (개발용)
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            headers.add("Access-Control-Max-Age", "3600");

            // Cache Control
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            log.debug("Security headers added to response");
            return chain.filter(exchange);
        };
    }
}