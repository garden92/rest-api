package com.kt.kol.api.encryption.repository;

import java.time.LocalDateTime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.kt.kol.api.encryption.model.EcodPlcyHstDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 암호화 정책 이력 Repository
 */
@Repository
public interface EcodPlcyHstRepository extends ReactiveCrudRepository<EcodPlcyHstDTO, String> {

    /**
     * 채널 ID와 필드명으로 현재 유효한 정책 조회 (sys_ 필드 제외)
     * @param chId 채널 ID
     * @param fieldNm 필드명
     * @return 현재 유효한 정책
     */
    @Query("""
        SELECT ch_id, field_nm, efct_st_dt, efct_fns_dt, field_desc_sbst
        FROM kolown.ecod_plcy_hst
        WHERE ch_id = :chId 
          AND field_nm = :fieldNm
          AND :currentTime BETWEEN efct_st_dt AND efct_fns_dt
        ORDER BY efct_st_dt DESC
        LIMIT 1
    """)
    Mono<EcodPlcyHstDTO> findCurrentValidPolicy(String chId, String fieldNm, LocalDateTime currentTime);

    /**
     * 채널 ID로 현재 유효한 모든 정책 조회 (sys_ 필드 제외)
     * @param chId 채널 ID
     * @return 현재 유효한 모든 정책 목록
     */
    @Query("""
        SELECT ch_id, field_nm, efct_st_dt, efct_fns_dt, field_desc_sbst
        FROM kolown.ecod_plcy_hst
        WHERE ch_id = :chId 
          AND :currentTime BETWEEN efct_st_dt AND efct_fns_dt
        ORDER BY field_nm, efct_st_dt DESC
    """)
    Flux<EcodPlcyHstDTO> findAllCurrentValidPoliciesByChannel(String chId, LocalDateTime currentTime);

    /**
     * 모든 채널의 현재 유효한 정책 조회 (sys_ 필드 제외)
     * @return 현재 유효한 모든 정책 목록
     */
    @Query("""
        SELECT ch_id, field_nm, efct_st_dt, efct_fns_dt, field_desc_sbst
        FROM kolown.ecod_plcy_hst
        WHERE :currentTime BETWEEN efct_st_dt AND efct_fns_dt
        ORDER BY ch_id, field_nm, efct_st_dt DESC
    """)
    Flux<EcodPlcyHstDTO> findAllCurrentValidPolicies(LocalDateTime currentTime);

    /**
     * 채널 ID와 필드명으로 정책 이력 조회 (sys_ 필드 제외)
     * @param chId 채널 ID
     * @param fieldNm 필드명
     * @return 해당 필드의 모든 정책 이력
     */
    @Query("""
        SELECT ch_id, field_nm, efct_st_dt, efct_fns_dt, field_desc_sbst
        FROM kolown.ecod_plcy_hst
        WHERE ch_id = :chId 
          AND field_nm = :fieldNm
        ORDER BY efct_st_dt DESC
    """)
    Flux<EcodPlcyHstDTO> findPolicyHistory(String chId, String fieldNm);
}