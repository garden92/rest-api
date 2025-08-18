package com.kt.kol.api.prechk.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.kt.kol.api.prechk.model.DummyDTO;
import com.kt.kol.api.prechk.service.RestGwPreChkService;
import com.kt.kol.common.model.ResponseStdVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Tag(name = "RESTGW", description = "RestGW API")
@RestController
@RequestMapping("/restGw/ckeckBeforeRoute")
@Slf4j
public class RestGwPreChkController {

    private final RestGwPreChkService restGwPreChkService;

    @Operation(summary = "REST Gateway 사전체크", description = """
            REST Gateway 라우팅 전 Bmon에 로그를 전송 후 사전체크를 수행합니다.
            - 채널 ID 및 허용 경로 검증
            - 허용 IP 주소 검증
            - 사용자 권한 검증
            - API Key 인증 검증
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사전체크 성공", content = @Content(mediaType = "application/json")),
    })
    @PostMapping
    public <T> Mono<ResponseStdVO<DummyDTO>> checkBeforeRoute(@RequestBody T inDTO, ServerWebExchange exchange) {

        return restGwPreChkService.checkBeforeRoute(inDTO, exchange);
    }

    @Operation(summary = "REST Gateway 사전체크", description = """
            REST Gateway 라우팅 전 사전체크를 수행합니다.
            - 채널 ID 및 허용 경로 검증
            - 허용 IP 주소 검증
            - 사용자 권한 검증
            - API Key 인증 검증
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사전체크 성공", content = @Content(mediaType = "application/json")),
    })
    @PostMapping("/v2")
    public Mono<ResponseStdVO<DummyDTO>> checkBeforeRoute(ServerWebExchange exchange) {
        return restGwPreChkService.checkBeforeRoute(exchange);
    }

}
