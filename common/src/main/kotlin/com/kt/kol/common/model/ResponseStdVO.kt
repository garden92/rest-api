package com.kt.kol.common.model

import java.time.LocalDateTime

data class ResponseStdVO<T>(
    val data: T?,
    val timestamp: LocalDateTime,
    val message: String,
    val success: Boolean
) {
    companion object {
        @JvmStatic
        fun <T> success(data: T): ResponseStdVO<T> {
            return ResponseStdVO(
                data = data,
                timestamp = LocalDateTime.now(),
                message = "처리성공",
                success = true
            )
        }

        @JvmStatic
        fun businessError(): ResponseStdVO<Void?> {
            return ResponseStdVO(
                data = null,
                timestamp = LocalDateTime.now(),
                message = "처리 오류가 발생했습니다",
                success = false
            )
        }

        @JvmStatic
        fun businessError(message: String): ResponseStdVO<Void?> {
            return ResponseStdVO(
                data = null,
                timestamp = LocalDateTime.now(),
                message = message,
                success = false
            )
        }

        @JvmStatic
        fun systemError(): ResponseStdVO<Void?> {
            return ResponseStdVO(
                data = null,
                timestamp = LocalDateTime.now(),
                message = "시스템 오류가 발생했습니다",
                success = false
            )
        }

        @JvmStatic
        fun <T> validationError(validationDetails: T? = null): ResponseStdVO<T> {
            return ResponseStdVO(
                data = validationDetails,
                timestamp = LocalDateTime.now(),
                message = "입력 값 검증 실패",
                success = false
            )
        }

        @JvmStatic
        fun notFound(message: String): ResponseStdVO<Void?> {
            return ResponseStdVO(
                data = null,
                timestamp = LocalDateTime.now(),
                message = message,
                success = false
            )
        }

        @JvmStatic
        fun <T> error(message: String): ResponseStdVO<T> {
            return ResponseStdVO(
                data = null,
                timestamp = LocalDateTime.now(),
                message = message,
                success = false
            )
        }
    }
}