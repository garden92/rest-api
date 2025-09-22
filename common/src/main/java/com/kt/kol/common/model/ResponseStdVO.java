package com.kt.kol.common.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseStdVO<T>(
		TrtErrInfoDTO trtErrInfoDTO,
		T data,
		LocalDateTime timestamp,
		boolean success) {

	public static <T> ResponseStdVO<T> success(T data) {
		return new ResponseStdVO<T>(
				new TrtErrInfoDTO("S", "KOLS0000", "정상 처리", "success"),
				data,
				LocalDateTime.now(),
				true);
	}

	public static <T> ResponseStdVO<T> success(TrtErrInfoDTO trtErrInfoDTO, T data) {
		return new ResponseStdVO<T>(
				trtErrInfoDTO,
				data,
				LocalDateTime.now(),
				true);
	}

	public static ResponseStdVO<Void> businessError(TrtErrInfoDTO trtErrInfoDTO) {
		return new ResponseStdVO<Void>(
				trtErrInfoDTO,
				null,
				LocalDateTime.now(),
				false);
	}

	public static ResponseStdVO<Void> systemError(TrtErrInfoDTO trtErrInfoDTO) {
		return new ResponseStdVO<Void>(
				trtErrInfoDTO,
				null,
				LocalDateTime.now(),
				false);
	}

	public static <T> ResponseStdVO<T> validationError(TrtErrInfoDTO trtErrInfoDTO, T validationDetails) {
		return new ResponseStdVO<T>(
				trtErrInfoDTO,
				validationDetails,
				LocalDateTime.now(),
				false);
	}

	public static ResponseStdVO<Void> notFound(String message) {
		return new ResponseStdVO<Void>(
				new TrtErrInfoDTO("N", "KOLS0404", "리소스를 찾을 수 없습니다", message),
				null,
				LocalDateTime.now(),
				false);
	}
}