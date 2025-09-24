package com.kt.kol.common.config

import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

/**
 * JWT 관련 중앙화된 설정 클래스
 * 토큰 생성과 검증에서 일관된 설정을 보장합니다.
 */
@Component
class JwtConfig(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") val expiration: Long
) {

    companion object {
        // JWT 알고리즘 - 한 곳에서 관리
        const val ALGORITHM = "HS256"
        const val HMAC_ALGORITHM = "HmacSHA256"

        // 토큰 클레임 키
        const val ROLES_CLAIM = "roles"
        const val USERNAME_CLAIM = "username"
    }

    val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    /**
     * Spring Security용 SecretKey 바이트 배열 반환
     */
    fun getSecretKeyBytes(): ByteArray {
        return secret.toByteArray(StandardCharsets.UTF_8)
    }
}