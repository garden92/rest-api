package com.kt.kol.api.bmon.service;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import com.kt.kol.api.bmon.util.BMONSender;
import com.kt.kol.common.model.RequestStdVO;
import com.kt.kol.common.util.HeaderConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class BmonSenderService {

    private final BMONSender bmonSender;

    // restGW 라우팅 전 사전체크
    public <T> Mono<Void> processBmonSend(RequestStdVO<T> inVO, ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();

        // BMON 연동 호출
        return bmonSender.sendBmonMot(request.getHeaders().getFirst(HeaderConstants.HEADER_TR_FLAG),
                inVO.data(),
                inVO.trtErrInfoDTO(), request);
    }

}
