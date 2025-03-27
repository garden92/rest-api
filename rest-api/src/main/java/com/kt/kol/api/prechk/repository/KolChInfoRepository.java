package com.kt.kol.api.prechk.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.kt.kol.api.prechk.model.KolChInfoDTO;

import reactor.core.publisher.Flux;

@Repository
public interface KolChInfoRepository extends ReactiveCrudRepository<KolChInfoDTO, String>{

	@Query("""
	  select a.ch_id,
	         a.ch_path_adr
	    from kolown.kol_ch_bas a,
	         kolown.kol_ch_info_bas b
	   where a.ch_id = b.ch_id 
	     and to_timestamp(:lgDateTime, 'yyyymmddhh24miss') BETWEEN a.efct_st_date AND a.efct_fns_date
	     and to_timestamp(:lgDateTime, 'yyyymmddhh24miss') BETWEEN b.efct_st_date AND b.efct_fns_date
	     and a.ch_id = :chId
			""")
	Flux<KolChInfoDTO> checkKolChInfo(String chId, String lgDateTime);
}
