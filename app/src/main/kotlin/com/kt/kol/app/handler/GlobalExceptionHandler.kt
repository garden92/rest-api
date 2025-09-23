package com.kt.kol.app.handler

import com.kt.kol.common.exception.BusinessException
import com.kt.kol.common.model.ResponseStdVO
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(value = [BusinessException::class])
    fun handleBizException(e: BusinessException): Mono<ResponseEntity<ResponseStdVO<Void?>>> {
        log.warn("Business Exception: {}", e.message)
        return Mono.just(
            ResponseEntity.status(HttpStatus.OK)
                .body(ResponseStdVO.error<Void?>(e.message ?: "Business error"))
        )
    }

    @ExceptionHandler(value = [WebExchangeBindException::class])
    fun handleValidationException(e: WebExchangeBindException): Mono<ResponseEntity<ResponseStdVO<Map<String, String>>>> {
        log.warn("Validation error: {}", e.message)
        val errors = mutableMapOf<String, String>()
        e.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Validation error"
            errors[fieldName] = errorMessage
        }

        return Mono.just(
            ResponseEntity.status(HttpStatus.OK)
                .body(ResponseStdVO.validationError(errors))
        )
    }

    @ExceptionHandler(value = [ServerWebInputException::class])
    fun handleServerWebInputException(e: ServerWebInputException): Mono<ResponseEntity<ResponseStdVO<Void?>>> {
        log.warn("Invalid input: {}", e.message)
        return Mono.just(
            ResponseEntity.status(HttpStatus.OK)
                .body(ResponseStdVO.error<Void?>(e.message ?: "Invalid input"))
        )
    }

    @ExceptionHandler(value = [ResponseStatusException::class])
    fun handleResponseStatusException(e: ResponseStatusException): Mono<ResponseEntity<ResponseStdVO<Void?>>> {
        log.warn("Response status exception: {}", e.message)
        return Mono.just(
            ResponseEntity.status(e.statusCode)
                .body(ResponseStdVO.error<Void?>(e.message ?: "Response status error"))
        )
    }

    @ExceptionHandler(value = [IllegalArgumentException::class])
    fun handleIllegalArgumentException(e: IllegalArgumentException): Mono<ResponseEntity<ResponseStdVO<Void?>>> {
        log.warn("Illegal argument: {}", e.message)
        return Mono.just(
            ResponseEntity.status(HttpStatus.OK)
                .body(ResponseStdVO.error<Void?>(e.message ?: "Illegal argument"))
        )
    }

    @ExceptionHandler(value = [RuntimeException::class])
    fun handleRuntimeException(e: RuntimeException): Mono<ResponseEntity<ResponseStdVO<Void?>>> {
        log.error("Runtime exception occurred: ", e)
        return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseStdVO.systemError())
        )
    }

    @ExceptionHandler(value = [Exception::class])
    fun handleException(e: Exception): Mono<ResponseEntity<ResponseStdVO<Void?>>> {
        log.error("Unexpected exception occurred: ", e)
        return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseStdVO.systemError())
        )
    }
}