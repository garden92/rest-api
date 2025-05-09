package com.kt.kol.api.prechk.model;

public record ApiKeyInfoInfoDTO (
	
	/**
	 * 채널아이디
	 */
	String chId,
	/**
	 * 사용자아이디
	 */
	String rqtSvcNm,
	/**
	 * API 키 값
	 */
	String apiKeyVal
) {}
