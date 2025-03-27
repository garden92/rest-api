package com.kt.kol.app.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kt.kol.common.exception.BusinessException;
import com.kt.kol.common.exception.BusinessExceptionWithReqeustBody;
import com.kt.kol.common.model.ResponseStdVO;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(value = BusinessException.class)
	@ResponseBody
	public ResponseStdVO<Void> handleBizExceptions(
			BusinessException e) {
		log.error("eer {}", e.getMessage());
		// response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		return ResponseStdVO.businessError(null);
	}

	@ExceptionHandler(value = BusinessExceptionWithReqeustBody.class)
	@ResponseBody
	public ResponseStdVO<?> handleBizExceptionsWithRequestData(
			BusinessExceptionWithReqeustBody e) {
		log.error("code :: {}", e.getErrorCode());
		log.error("data :: " + e.getRequestData().toString());
		return ResponseStdVO.businessError(null);
	}

}
