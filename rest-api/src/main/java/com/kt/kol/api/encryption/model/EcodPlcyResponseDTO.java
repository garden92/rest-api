package com.kt.kol.api.encryption.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 암호화 정책 응답 DTO
 * sys_ 필드는 제외하고 사용자에게 필요한 정보만 제공
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcodPlcyResponseDTO {

    private String chId;
    private String fieldNm;
    private LocalDateTime efctStDt;
    private LocalDateTime efctFnsDt;
    private String fieldDescSbst;

    /**
     * EcodPlcyHstDTO에서 사용자용 응답 DTO로 변환
     */
    public static EcodPlcyResponseDTO from(EcodPlcyHstDTO hstDTO) {
        return new EcodPlcyResponseDTO(
            hstDTO.getChId(),
            hstDTO.getFieldNm(),
            hstDTO.getEfctStDt(),
            hstDTO.getEfctFnsDt(),
            hstDTO.getFieldDescSbst()
        );
    }

    /**
     * 현재 시간 기준으로 유효한지 확인
     */
    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        return (efctStDt == null || efctStDt.isBefore(now) || efctStDt.isEqual(now)) &&
               (efctFnsDt == null || efctFnsDt.isAfter(now) || efctFnsDt.isEqual(now));
    }
}