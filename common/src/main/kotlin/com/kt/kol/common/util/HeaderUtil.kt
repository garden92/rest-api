package com.kt.kol.common.util

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.util.context.Context

@Component
object HeaderUtil {

    private const val SERVER_WEB_EXCHANGE_KEY = "SERVER_WEB_EXCHANGE"

    fun getCurrRequest(): Mono<ServerHttpRequest> {
        return Mono.deferContextual { contextView ->
            val exchange = contextView.get<ServerWebExchange>(SERVER_WEB_EXCHANGE_KEY)
            Mono.just(exchange.request)
        }
    }

    fun getHeader(headerName: String): Mono<String?> {
        return getCurrRequest()
            .map { request -> request.headers.getFirst(headerName) }
    }

    fun getRequestURI(): Mono<String> {
        return getCurrRequest()
            .map { request -> request.uri.path }
    }

    fun getRemoteAddr(): Mono<String> {
        return getCurrRequest()
            .map { request ->
                val xForwardedFor = request.headers.getFirst("X-Forwarded-For")
                if (!xForwardedFor.isNullOrEmpty()) {
                    return@map xForwardedFor.split(",")[0].trim()
                }

                val xRealIp = request.headers.getFirst("X-Real-IP")
                if (!xRealIp.isNullOrEmpty()) {
                    return@map xRealIp
                }

                request.remoteAddress?.address?.hostAddress ?: "unknown"
            }
    }

    fun withServerWebExchange(context: Context, exchange: ServerWebExchange): Context {
        return context.put(SERVER_WEB_EXCHANGE_KEY, exchange)
    }

    // Kotlin Coroutines를 위한 suspend 함수들 추가
    suspend fun getCurrRequestSuspend(): ServerHttpRequest {
        return getCurrRequest().awaitSingle()
    }

    suspend fun getHeaderSuspend(headerName: String): String? {
        return getHeader(headerName).awaitSingle()
    }

    suspend fun getRequestURISuspend(): String {
        return getRequestURI().awaitSingle()
    }

    suspend fun getRemoteAddrSuspend(): String {
        return getRemoteAddr().awaitSingle()
    }
}