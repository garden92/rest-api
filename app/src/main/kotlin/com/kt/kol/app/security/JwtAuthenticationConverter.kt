package com.kt.kol.app.security

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationConverter(
    private val jwtTokenProvider: JwtTokenProvider
) : ServerAuthenticationConverter {

    private val log = LoggerFactory.getLogger(JwtAuthenticationConverter::class.java)

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.fromCallable {
            val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return@fromCallable null
            }

            val token = authHeader.substring(7)

            if (!jwtTokenProvider.validateToken(token)) {
                log.debug("Invalid JWT token")
                return@fromCallable null
            }

            val username = jwtTokenProvider.getUsername(token)
            val roles = jwtTokenProvider.getRoles(token)
            val userId = jwtTokenProvider.getUserId(token)

            val authorities = roles.map { role ->
                SimpleGrantedAuthority("ROLE_$role")
            }

            val principal = JwtUserPrincipal(userId, username, roles)

            UsernamePasswordAuthenticationToken(principal, null, authorities)
        }
    }
}