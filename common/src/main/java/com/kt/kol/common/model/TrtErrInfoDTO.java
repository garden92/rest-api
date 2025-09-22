package com.kt.kol.common.model;

public record TrtErrInfoDTO(
		String responseType, // 응답유형(I : 정상, 시스템 에러 : S, 비즈니스 에러 : E)
		String responseCode, // 응답코드(개발자 참조용)
		String responseBasc, // 응답기본(개발자 참조용)
		String responseDtal // 응답상세(개발자 참조용)
) {

	public static TrtErrInfoDTO error(Exception e) {
		return new TrtErrInfoDTO(
				"S",
				"SYSTEM_ERR",
				"KOS-O/L 시스템 오류",
				e.getMessage());
	}
}
