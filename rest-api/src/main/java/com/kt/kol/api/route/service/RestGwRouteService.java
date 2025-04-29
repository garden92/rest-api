package com.kt.kol.api.route.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kt.kol.api.route.repository.RestGwRouteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestGwRouteService {

    private final RestGwRouteRepository restGwRouteRepository;
    
    public Mono<List<Map<String, Object>>> retrieveRouteInfo() {		
		
        return restGwRouteRepository.retrieveRouteInfo();
	}
}
