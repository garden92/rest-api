package com.kt.kol.api.encryption.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 암호화 정책 이력 DTO
 * 새로운 통합 테이블 구조에 맞춘 DTO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("kolown.ecod_plcy_hst")
public class EcodPlcyHstDTO {

    @Column("ch_id")
    private String chId;

    @Column("field_nm")
    private String fieldNm;

    @Column("efct_st_dt")
    private LocalDateTime efctStDt;

    @Column("efct_fns_dt")
    private LocalDateTime efctFnsDt;

    @Column("field_desc_sbst")
    private String fieldDescSbst;

    /**
     * 현재 시간 기준으로 유효한지 확인
     * @return 현재 유효한 정책이면 true
     */
    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        return (efctStDt == null || efctStDt.isBefore(now) || efctStDt.isEqual(now)) &&
               (efctFnsDt == null || efctFnsDt.isAfter(now) || efctFnsDt.isEqual(now));
    }

    /**
     * 특정 시점 기준으로 유효한지 확인
     * @param checkTime 확인할 시점
     * @return 해당 시점에 유효한 정책이면 true
     */
    public boolean isValidAt(LocalDateTime checkTime) {
        if (checkTime == null) return false;
        return (efctStDt == null || efctStDt.isBefore(checkTime) || efctStDt.isEqual(checkTime)) &&
               (efctFnsDt == null || efctFnsDt.isAfter(checkTime) || efctFnsDt.isEqual(checkTime));
    }
}