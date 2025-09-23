package com.kt.kol.common.exception

class BusinessException : RuntimeException {

    val errorCode: String?

    constructor(message: String) : super(message) {
        this.errorCode = null
    }

    constructor(errorCode: String, message: String) : super(message) {
        this.errorCode = errorCode
    }

    constructor(message: String, cause: Throwable) : super(message, cause) {
        this.errorCode = null
    }

    constructor(errorCode: String, message: String, cause: Throwable) : super(message, cause) {
        this.errorCode = errorCode
    }

    companion object {
        private const val serialVersionUID = -2291025218301636662L
    }
}