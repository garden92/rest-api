package com.kt.kol.app.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kt.kol.common.exception.BusinessException;
import com.kt.kol.common.model.ResponseStdVO;
import com.kt.kol.common.model.TrtErrInfoDTO;
import com.kt.kol.common.util.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(value = BusinessException.class)
	@ResponseBody
	public ResponseStdVO<Void> handleBizException(
			BusinessException e) {
		return ResponseStdVO.businessError(e.getTrtErrInfoDTO());
	}
	
	// RuntimeException 처리
	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public ResponseStdVO<Void> handleException(Exception e) {
		log.error(StringUtil.printExceptionStack(e));
		return ResponseStdVO.systemError(new TrtErrInfoDTO("S", "KOLS0001"
							, e.getMessage(), StringUtil.printExceptionStack(e)));
	}
}