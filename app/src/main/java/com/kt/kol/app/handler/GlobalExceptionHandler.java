package com.kt.kol.app.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kt.kol.common.exception.BusinessException;
import com.kt.kol.common.model.ResponseStdVO;
import com.kt.kol.common.model.TrtErrInfoDTO;
import com.kt.kol.common.util.StringUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(value = BusinessException.class)
	public Mono<ResponseStdVO<Void>> handleBizException(
			BusinessException e) {
		return Mono.just(ResponseStdVO.businessError(e.getTrtErrInfoDTO()));
	}
	
	// RuntimeException 처리
	@ExceptionHandler(value = Exception.class)
	public Mono<ResponseStdVO<Void>> handleException(Exception e) {
		log.error(StringUtil.printExceptionStack(e));
		return Mono.just(ResponseStdVO.systemError(new TrtErrInfoDTO("S", "KOLS0001"
							, e.getMessage(), StringUtil.printExceptionStack(e))));
	}
}