package com.kt.kol.api.bmon.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.kt.kol.api.bmon.service.BmonSenderService;
import com.kt.kol.common.model.RequestStdVO;

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
@RequestMapping("/restGw/processBmonSend")
@Slf4j
public class BmonSenderController {

    private final BmonSenderService bmonSenderService;

    @Operation(summary = "restGW용 BMON연동", description = "restGW용 BMON연동 처리를 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "체크 성공", content = @Content(mediaType = "application/json"))
    })
    @PostMapping()
    public <T> Mono<Void> processBmonSend(@RequestBody RequestStdVO<T> inVO, ServerWebExchange exchange) {

        return bmonSenderService.processBmonSend(inVO, exchange);
    }

}
