package com.kt.kol.app.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import com.kt.kol.common.exception.BusinessException;
import com.kt.kol.common.model.ResponseStdVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(value = BusinessException.class)
	public Mono<ResponseEntity<ResponseStdVO<Void>>> handleBizException(BusinessException e) {
		log.warn("Business Exception: {}", e.getMessage());
		return Mono.just(ResponseEntity.status(HttpStatus.OK)
				.body(ResponseStdVO.businessError(e.getMessage())));
	}

	@ExceptionHandler(value = WebExchangeBindException.class)
	public Mono<ResponseEntity<ResponseStdVO<Map<String, String>>>> handleValidationException(
			WebExchangeBindException e) {
		log.warn("Validation error: {}", e.getMessage());
		Map<String, String> errors = new HashMap<>();
		e.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		return Mono.just(ResponseEntity.status(HttpStatus.OK)
				.body(ResponseStdVO.validationError(errors)));
	}

	@ExceptionHandler(value = ServerWebInputException.class)
	public Mono<ResponseEntity<ResponseStdVO<Void>>> handleServerWebInputException(ServerWebInputException e) {
		log.warn("Invalid input: {}", e.getMessage());
		return Mono.just(ResponseEntity.status(HttpStatus.OK)
				.body(ResponseStdVO.businessError(e.getMessage())));
	}

	@ExceptionHandler(value = ResponseStatusException.class)
	public Mono<ResponseEntity<ResponseStdVO<Void>>> handleResponseStatusException(ResponseStatusException e) {
		log.warn("Response status exception: {}", e.getMessage());
		return Mono.just(ResponseEntity.status(e.getStatusCode())
				.body(ResponseStdVO.businessError(e.getMessage())));
	}

	@ExceptionHandler(value = IllegalArgumentException.class)
	public Mono<ResponseEntity<ResponseStdVO<Void>>> handleIllegalArgumentException(IllegalArgumentException e) {
		log.warn("Illegal argument: {}", e.getMessage());
		return Mono.just(ResponseEntity.status(HttpStatus.OK)
				.body(ResponseStdVO.businessError(e.getMessage())));
	}

	@ExceptionHandler(value = RuntimeException.class)
	public Mono<ResponseEntity<ResponseStdVO<Void>>> handleRuntimeException(RuntimeException e) {
		log.error("Runtime exception occurred: ", e);
		return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ResponseStdVO.systemError()));
	}

	@ExceptionHandler(value = Exception.class)
	public Mono<ResponseEntity<ResponseStdVO<Void>>> handleException(Exception e) {
		log.error("Unexpected exception occurred: ", e);
		return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ResponseStdVO.systemError()));
	}
}