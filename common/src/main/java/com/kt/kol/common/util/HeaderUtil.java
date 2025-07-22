package com.kt.kol.common.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
public class HeaderUtil {

    private static final String SERVER_WEB_EXCHANGE_KEY = "SERVER_WEB_EXCHANGE";

    public static Mono<ServerHttpRequest> getCurrRequest() {
        return Mono.deferContextual(contextView -> {
            ServerWebExchange exchange = contextView.get(SERVER_WEB_EXCHANGE_KEY);
            return Mono.just(exchange.getRequest());
        });
    }

    public static Mono<String> getHeader(String headerName) {
        return getCurrRequest()
                .map(request -> request.getHeaders().getFirst(headerName));
    }

    public static Mono<String> getGlobalNo() {
        return getHeader(HeaderConstants.GLOBAL_NO);
    }

    public static Mono<String> getChnlType() {
        return getHeader(HeaderConstants.CHNL_TYPE);
    }

    public static Mono<String> getUserId() {
        return getHeader(HeaderConstants.USER_ID);
    }

    public static Mono<String> getOrgId() {
        return getHeader(HeaderConstants.ORG_ID);
    }

    public static Mono<String> getSrcId() {
        return getHeader(HeaderConstants.SRC_ID);
    }

    public static Mono<String> getCmpnCd() {
        return getHeader(HeaderConstants.CMPN_CD);
    }

    public static Mono<String> getLgDateTime() {
        return getHeader(HeaderConstants.LG_DATE_TIME);
    }

    public static Mono<String> getTrFlag() {
        return getHeader(HeaderConstants.TR_FLAG);
    }

    public static Mono<String> getRequestURI() {
        return getCurrRequest()
                .map(request -> request.getURI().getPath());
    }

    public static Mono<String> getRemoteAddr() {
        return getCurrRequest()
                .map(request -> {
                    String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
                    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                        return xForwardedFor.split(",")[0].trim();
                    }
                    String xRealIp = request.getHeaders().getFirst("X-Real-IP");
                    if (xRealIp != null && !xRealIp.isEmpty()) {
                        return xRealIp;
                    }
                    return request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress()
                            : "unknown";
                });
    }

    public static Context withServerWebExchange(Context context, ServerWebExchange exchange) {
        return context.put(SERVER_WEB_EXCHANGE_KEY, exchange);

    }

}
