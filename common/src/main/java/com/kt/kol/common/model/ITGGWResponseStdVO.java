package com.kt.kol.common.model;

import com.fasterxml.jackson.databind.JsonNode;

public record ITGGWResponseStdVO(
		ResponseType responseType,

		String responseCode,

		String responseTitle,

		String responseBasc,

		String responseDtal,
		String responseSystem,

		JsonNode data) {
	// 응답 유형을 enum으로 정의
	public enum ResponseType {
		I("정상"),
		E("비즈니스에러"),
		S("시스템에러");

		private final String description;

		ResponseType(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}
}
