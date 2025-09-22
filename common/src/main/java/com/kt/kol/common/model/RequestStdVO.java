package com.kt.kol.common.model;

public record RequestStdVO<T>(
		T data) {

	public static <T> RequestStdVO<T> makeReq(T data) {
		return new RequestStdVO<T>(
				data);
	}
}
