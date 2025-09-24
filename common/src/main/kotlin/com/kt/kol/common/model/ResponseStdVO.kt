package com.kt.kol.common.model

import java.time.LocalDateTime

data class ResponseStdVO<T>(
    val data: T?,
    val timestamp: LocalDateTime,
    val message: String,
    val success: Boolean
) {
    companion object {
        fun <T> success(data: T): ResponseStdVO<T> = ResponseStdVO(
            data = data,
            timestamp = LocalDateTime.now(),
            message = "처리성공",
            success = true
        )

        fun businessError(): ResponseStdVO<Void?> = ResponseStdVO(
            data = null,
            timestamp = LocalDateTime.now(),
            message = "처리 오류가 발생했습니다",
            success = false
        )

        fun businessError(message: String): ResponseStdVO<Void?> = ResponseStdVO(
            data = null,
            timestamp = LocalDateTime.now(),
            message = message,
            success = false
        )

        fun systemError(): ResponseStdVO<Void?> = ResponseStdVO(
            data = null,
            timestamp = LocalDateTime.now(),
            message = "시스템 오류가 발생했습니다",
            success = false
        )

        fun <T> validationError(validationDetails: T? = null): ResponseStdVO<T> = ResponseStdVO(
            data = validationDetails,
            timestamp = LocalDateTime.now(),
            message = "입력 값 검증 실패",
            success = false
        )

        fun notFound(message: String): ResponseStdVO<Void?> = ResponseStdVO(
            data = null,
            timestamp = LocalDateTime.now(),
            message = message,
            success = false
        )

        fun <T> error(message: String): ResponseStdVO<T> = ResponseStdVO(
            data = null,
            timestamp = LocalDateTime.now(),
            message = message,
            success = false
        )
    }
}