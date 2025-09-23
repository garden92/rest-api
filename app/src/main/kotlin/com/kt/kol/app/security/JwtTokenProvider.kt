package com.kt.kol.app.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret:defaultSecretKeyForDevlopment1234567890}")
    secret: String
) {
    private val log = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            log.debug("JWT validation failed: {}", e.message)
            false
        }
    }

    fun getUsername(token: String): String {
        return getClaims(token).subject
    }

    @Suppress("UNCHECKED_CAST")
    fun getRoles(token: String): List<String> {
        val claims = getClaims(token)
        return claims.get("roles", List::class.java) as List<String>
    }

    fun getUserId(token: String): String {
        val claims = getClaims(token)
        return claims.get("userId", String::class.java)
    }

    fun isTokenExpired(token: String): Boolean {
        return getClaims(token).expiration.before(Date())
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}