package com.kt.kol.api.route.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.kol.api.route.service.RestGwRouteService;

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
@RequestMapping("/restGw/retrieveRouteInfo")
@Slf4j
public class RestGwRouteController {

    private final RestGwRouteService restGwRouteService;

    @Operation(summary = "restGW용 라우트정보조회", description = "restGW용 라우트정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public Mono<List<Map<String, Object>>> retrieveRouteInfo() {
        return restGwRouteService.retrieveRouteInfo();
    }
}
