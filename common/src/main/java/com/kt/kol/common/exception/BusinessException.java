package com.kt.kol.common.exception;

import com.kt.kol.common.model.TrtErrInfoDTO;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = -2291025218301636662L;

	// TODO : TrtBaseInfoDTO 제거
	// private TrtBaseInfoDTO trtBaseInfoDTO;
	private TrtErrInfoDTO trtErrInfoDTO;

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(TrtErrInfoDTO trtErrInfoDTO) {
		this.trtErrInfoDTO = trtErrInfoDTO;
	}

	public TrtErrInfoDTO getTrtErrInfoDTO() {
		return trtErrInfoDTO;
	}
}