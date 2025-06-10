package com.kt.kol.api.prechk.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.kt.kol.api.prechk.model.KolChUserInfoDTO;

import reactor.core.publisher.Flux;

@Repository
public interface KolChUserInfoRepository extends ReactiveCrudRepository<KolChUserInfoDTO, String>{

	@Query("""
	  select ch_id,
	         user_id
	    from kolown.kol_ch_user_id_list_bas
	   where ch_id = :chId
	     and (user_id = :userId or user_id = '*')
			""")
	Flux<KolChUserInfoDTO> checkKolChUserInfo(String chId, String userId);
}
