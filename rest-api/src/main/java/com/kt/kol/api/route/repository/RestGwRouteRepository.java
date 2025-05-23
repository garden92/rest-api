package com.kt.kol.api.route.repository;

import java.util.List;
import java.util.Map;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public class RestGwRouteRepository {
    private final DatabaseClient databaseClient;

    public RestGwRouteRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<List<Map<String, Object>>> retrieveRouteInfo() {
        return databaseClient.sql("""
                    select rqt_svc_nm,
                           route_svc_nm,
                           route_id,
                           srvr_id
                    from kolown.route_info_bas
                    where now() between efct_st_dt and efct_fns_dt 
                """)
                .fetch()
                .all()
                .collectList();
    }
    
}