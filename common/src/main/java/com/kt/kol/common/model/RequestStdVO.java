package com.kt.kol.common.model;

public record RequestStdVO<T>(
		TrtErrInfoDTO trtErrInfoDTO,
		T data) {
	
	public static <T> RequestStdVO<T> makeReq(TrtErrInfoDTO trtErrInfoDTO, T data) {
		return new RequestStdVO<T>(
				trtErrInfoDTO,
				data);
	}
}
