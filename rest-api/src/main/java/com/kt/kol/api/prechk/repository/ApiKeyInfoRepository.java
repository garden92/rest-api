package com.kt.kol.api.prechk.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.kt.kol.api.prechk.model.ApiKeyInfoInfoDTO;

import reactor.core.publisher.Flux;

@Repository
public interface ApiKeyInfoRepository extends ReactiveCrudRepository<ApiKeyInfoInfoDTO, String>{

	@Query("""
	  select ch_id,
			 rqt_svc_nm
		from kolown.api_key_info_bas
	   where ch_id = :chId
		 and rqt_svc_nm  = :rqtSvcNm
		 and to_timestamp(:lgDateTime, 'yyyymmddhh24miss') between efct_st_dt and efct_fns_dt
		 and api_key_val = :apKeyVal
			""")
	Flux<ApiKeyInfoInfoDTO> checkApiKeyInfo(String chId, String rqtSvcNm, String lgDateTime, String apKeyVal);
}
