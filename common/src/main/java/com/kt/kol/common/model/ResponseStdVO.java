package com.kt.kol.common.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "KOL ITG 공통 응답 모델")
public record ResponseStdVO<T>(
		@Schema(title = "처리 에러 정보") TrtErrInfoDTO trtErrInfoDTO,
		@Schema(title = "응답 데이터") T data) {

	public static <T> ResponseStdVO<T> success(TrtErrInfoDTO trtErrInfoDTO, T data) {
		return new ResponseStdVO<T>(
				trtErrInfoDTO,
				data);
	}

	public static ResponseStdVO<Void> businessError(TrtErrInfoDTO trtErrInfoDTO) {
		return new ResponseStdVO<Void>(
				trtErrInfoDTO,
				null);
	}

	public static ResponseStdVO<Void> systemError(TrtErrInfoDTO trtErrInfoDTO) {
		return new ResponseStdVO<Void>(
				trtErrInfoDTO,
				null);
	}
}
