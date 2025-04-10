package com.kt.kol.app.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kt.kol.common.exception.BusinessException;
import com.kt.kol.common.model.ResponseStdVO;

@ControllerAdvice
public class GlobalExceptionHandler {

	// TODO : TrtBaseInfoDTO 제거
	@ExceptionHandler(value = BusinessException.class)
	@ResponseBody
	public ResponseStdVO<Void> handleBizException(
			BusinessException e) {
		return ResponseStdVO.businessError(e.getTrtErrInfoDTO());
	}

}