package com.kt.kol.app.config

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.server.WebFilter
import reactor.util.context.Context
import java.time.Duration
import java.time.Instant
import java.util.*

@Configuration
class LoggingWebFilter {

    private val log = LoggerFactory.getLogger(LoggingWebFilter::class.java)

    @Bean
    fun requestResponseLoggingFilter(): WebFilter {
        return WebFilter { exchange, chain ->
            if (shouldSkipLogging(exchange.request)) {
                return@WebFilter chain.filter(exchange)
            }

            val correlationId = UUID.randomUUID().toString().substring(0, 8)
            val startTime = Instant.now()

            val request = exchange.request

            // MDC에 correlation ID 설정
            chain.filter(exchange)
                .contextWrite(Context.of("correlationId", correlationId))
                .doOnSubscribe { subscription ->
                    MDC.put("correlationId", correlationId)
                    logRequest(request, correlationId)
                }
                .doFinally { signalType ->
                    val endTime = Instant.now()
                    val duration = Duration.between(startTime, endTime)
                    val response = exchange.response
                    logResponse(response, correlationId, duration)
                    MDC.clear()
                }
        }
    }

    private fun logRequest(request: ServerHttpRequest, correlationId: String) {
        log.info(
            "[{}] --> {} {} | Remote: {} | User-Agent: {}",
            correlationId,
            request.method,
            request.uri,
            getClientIp(request),
            request.headers.getFirst("User-Agent")
        )
    }

    private fun logResponse(response: ServerHttpResponse, correlationId: String, duration: Duration) {
        log.info(
            "[{}] <-- {} | Duration: {}ms",
            correlationId,
            response.statusCode,
            duration.toMillis()
        )
    }

    private fun getClientIp(request: ServerHttpRequest): String {
        val xForwardedFor = request.headers.getFirst("X-Forwarded-For")
        if (!xForwardedFor.isNullOrEmpty()) {
            return xForwardedFor.split(",")[0].trim()
        }

        val xRealIp = request.headers.getFirst("X-Real-IP")
        if (!xRealIp.isNullOrEmpty()) {
            return xRealIp
        }

        return request.remoteAddress?.address?.hostAddress ?: "unknown"
    }

    private fun shouldSkipLogging(request: ServerHttpRequest): Boolean {
        val path = request.uri.path
        return path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".ico")
    }
}