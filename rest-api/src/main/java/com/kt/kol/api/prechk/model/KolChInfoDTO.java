package com.kt.kol.api.prechk.model;

public record KolChInfoDTO (
	
	/**
	 * 채널아이디
	 */
	String chId,
	/**
	 * 채널경로주소
	 */
	String chPathAdr,
	/**
	 * 유효시작일자
	 */
	String efctStDate,
	/**
	 * 유효종료일자
	 */
	String efctFnsDate,
	/**
	 * 채널설명내용
	 */
	String ch_desc_sbst
) {}
