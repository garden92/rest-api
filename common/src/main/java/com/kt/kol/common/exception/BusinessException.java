package com.kt.kol.common.exception;

import com.kt.kol.common.model.TrtErrInfoDTO;
import com.kt.kol.common.util.HeaderUtil;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = -2291025218301636662L;

	private String errorCode;
	private TrtErrInfoDTO trtErrInfoDTO;

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(TrtErrInfoDTO trtErrInfoDTO) {
		super(HeaderUtil.getGlobalNo() + " || " + trtErrInfoDTO.responseBasc());
		this.trtErrInfoDTO = trtErrInfoDTO;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public TrtErrInfoDTO getTrtErrInfoDTO() {
		return trtErrInfoDTO;
	}
}