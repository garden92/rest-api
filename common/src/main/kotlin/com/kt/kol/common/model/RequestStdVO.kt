package com.kt.kol.common.model

data class RequestStdVO<T>(
    val data: T
) {
    companion object {
        fun <T> makeReq(data: T): RequestStdVO<T> {
            return RequestStdVO(data)
        }
    }
}