package com.kt.kol.common.util;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class HeaderUtil {
	
	public static HttpServletRequest getCurrRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
	}
	
	public static String getHeader(String headerName) {
		return getCurrRequest().getHeader(headerName);
	}

	public static String getGlobalNo() {
		return getCurrRequest().getHeader(HeaderConstants.GLOBAL_NO);
	}
	
	public static String getChnlType() {
		return getCurrRequest().getHeader(HeaderConstants.CHNL_TYPE);
	}
	
	public static String getUserId() {
		return getCurrRequest().getHeader(HeaderConstants.USER_ID);
	}
	
	public static String getOrgId() {
		return getCurrRequest().getHeader(HeaderConstants.ORG_ID);
	}

	public static String getSrcId() {
		return getCurrRequest().getHeader(HeaderConstants.SRC_ID);
	}
	
	public static String getCmpnCd() {
		return getCurrRequest().getHeader(HeaderConstants.CMPN_CD);
	}
	
	public static String getLgDateTime() {
		return getCurrRequest().getHeader(HeaderConstants.LG_DATE_TIME);
	}
	
	public static String getTrFlag() {
		return getCurrRequest().getHeader(HeaderConstants.TR_FLAG);
	}
	
	public static String getRequestURI() {
		return getCurrRequest().getRequestURI();
	}
	
	public static String getRemoteAddr() {
		return getCurrRequest().getRemoteAddr();
	}
}
