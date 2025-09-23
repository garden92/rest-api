package com.kt.kol.common.annotation

/**
 * 통합 로그 어노테이션
 * 메서드에 적용하여 로깅 ID를 지정할 수 있습니다.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class IntegrationLog(
    val id: String = ""
)