package com.kt.kol.common.exception;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = -2291025218301636662L;

	private String errorCode;

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

	public String getErrorCode() {
		return errorCode;
	}

}