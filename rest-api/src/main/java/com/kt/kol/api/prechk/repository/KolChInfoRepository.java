package com.kt.kol.api.prechk.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.kt.kol.api.prechk.model.KolChInfoDTO;

import reactor.core.publisher.Flux;

@Repository
public interface KolChInfoRepository extends ReactiveCrudRepository<KolChInfoDTO, String>{

	// Mock 프로파일용 H2 호환 쿼리 (PARSEDATETIME 사용)
	@Query("""
	  select a.ch_id,
	         a.ch_path_adr
	    from kolown.kol_ch_bas a,
	         kolown.kol_ch_info_bas b
	   where a.ch_id = b.ch_id 
	     and PARSEDATETIME(:lgDateTime, 'yyyyMMddHHmmss') BETWEEN a.efct_st_date AND a.efct_fns_date
	     and PARSEDATETIME(:lgDateTime, 'yyyyMMddHHmmss') BETWEEN b.efct_st_date AND b.efct_fns_date
	     and a.ch_id = :chId
			""")
	Flux<KolChInfoDTO> checkKolChInfo(String chId, String lgDateTime);
}
