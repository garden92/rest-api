package com.kt.kol.common.service

import com.kt.kol.common.config.JwtConfig
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * JWT 토큰 생성 및 검증을 담당하는 중앙화된 서비스
 * 모든 JWT 관련 로직을 한 곳에서 관리합니다.
 */
@Service
class JwtTokenService(
    private val jwtConfig: JwtConfig
) {

    companion object {
        private val log = LoggerFactory.getLogger(JwtTokenService::class.java)
    }

    /**
     * JWT 토큰 생성 (범용)
     */
    fun generateToken(subject: String, roles: List<String>, additionalClaims: Map<String, Any>? = null): String {
        val now = Instant.now()
        val expiry = now.plus(jwtConfig.expiration, ChronoUnit.SECONDS)

        val builder = Jwts.builder()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .claim(JwtConfig.ROLES_CLAIM, roles)
            .signWith(jwtConfig.secretKey)

        additionalClaims?.forEach { (key, value) ->
            builder.claim(key, value)
        }

        return builder.compact()
    }

    /**
     * 기본 사용자 토큰 생성 (USER 권한)
     */
    fun generateUserToken(username: String): String {
        return generateToken(username, listOf("USER"), mapOf(JwtConfig.USERNAME_CLAIM to username))
    }

    /**
     * 관리자 토큰 생성 (USER, ADMIN 권한)
     */
    fun generateAdminToken(username: String): String {
        return generateToken(username, listOf("USER", "ADMIN"), mapOf(JwtConfig.USERNAME_CLAIM to username))
    }

    /**
     * 테스트용 토큰 생성
     */
    fun generateTestToken(username: String, roles: List<String>): String {
        return generateToken(username, roles, mapOf(
            JwtConfig.USERNAME_CLAIM to username,
            "test" to true,
            "issuer" to "kol-rest-api-test"
        ))
    }

    /**
     * 토큰에서 클레임 추출
     */
    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(jwtConfig.secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            getClaimsFromToken(token)
            true
        } catch (e: Exception) {
            log.error("Invalid JWT token: {}", e.message)
            false
        }
    }

    /**
     * 토큰에서 사용자명 추출
     */
    fun getUsernameFromToken(token: String): String? {
        val claims = getClaimsFromToken(token)
        return claims[JwtConfig.USERNAME_CLAIM] as? String
    }

    /**
     * 토큰에서 권한 목록 추출
     */
    @Suppress("UNCHECKED_CAST")
    fun getRolesFromToken(token: String): List<String>? {
        val claims = getClaimsFromToken(token)
        return claims[JwtConfig.ROLES_CLAIM] as? List<String>
    }
}