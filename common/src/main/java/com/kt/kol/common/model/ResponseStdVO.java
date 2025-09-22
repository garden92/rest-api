package com.kt.kol.common.model;

import java.time.LocalDateTime;

public record ResponseStdVO<T>(
		T data,
		LocalDateTime timestamp,
		String message,
		boolean success) {

	public static <T> ResponseStdVO<T> success(T data) {
		return new ResponseStdVO<T>(
				data,
				LocalDateTime.now(),
				"처리성공",
				true);
	}

	public static ResponseStdVO<Void> businessError() {
		return new ResponseStdVO<Void>(
				null,
				LocalDateTime.now(),
				"처리 오류가 발생했습니다",
				false);
	}

	public static ResponseStdVO<Void> businessError(String message) {
		return new ResponseStdVO<Void>(
				null,
				LocalDateTime.now(),
				message,
				false);
	}

	public static ResponseStdVO<Void> systemError() {
		return new ResponseStdVO<Void>(
				null,
				LocalDateTime.now(),
				"시스템 오류가 발생했습니다",
				false);
	}

	public static <T> ResponseStdVO<T> validationError(T validationDetails) {
		return new ResponseStdVO<T>(
				null,
				LocalDateTime.now(),
				"입력 값 검증 실패",
				false);
	}

	public static ResponseStdVO<Void> notFound(String message) {
		return new ResponseStdVO<Void>(
				null,
				LocalDateTime.now(),
				message,
				false);
	}
}