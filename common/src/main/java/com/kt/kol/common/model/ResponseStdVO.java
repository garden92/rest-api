package com.kt.kol.common.model;

public record ResponseStdVO<T>(
		TrtErrInfoDTO trtErrInfoDTO,
		T data) {

	// TODO: TrtBaseInfo 제거
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