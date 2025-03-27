package com.kt.kol.common.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "KOL ITG 공통 요청 모델")
public record RequestStdVO<T>(
		@Schema(title = "처리 에러 정보") TrtErrInfoDTO trtErrInfoDTO,
		@Schema(title = "API 요청 데이터") T data) {
	
	public static <T> RequestStdVO<T> makeReq(TrtErrInfoDTO trtErrInfoDTO, T data) {
		return new RequestStdVO<T>(
				trtErrInfoDTO,
				data);
	}
}
