package com.kt.kol.api.prechk.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.kt.kol.api.prechk.model.KolChIpInfoDTO;

import reactor.core.publisher.Flux;

@Repository
public interface KolChIpInfoRepository extends ReactiveCrudRepository<KolChIpInfoDTO, String>{

	@Query("""
	  select ch_id,
	         ip_adr
	    from kolown.kol_ch_ip_list_bas
	   where ch_id = :chId
	     and (ip_adr = :ipAdr or ip_adr = '*')
			""")
	Flux<KolChIpInfoDTO> checkKolChIpInfo(String chId, String ipAdr);
}
