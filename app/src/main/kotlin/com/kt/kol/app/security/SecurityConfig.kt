package com.kt.kol.app.security

import com.kt.kol.common.config.JwtConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import reactor.core.publisher.Mono
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        jwtDecoder: ReactiveJwtDecoder,
        jwtAuthConverter: Converter<Jwt, out Mono<out AbstractAuthenticationToken>>
    ): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .authorizeExchange { ex ->
                ex
                    .pathMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                    .pathMatchers(
                        "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**",
                        "/api-docs/**",
                        "/swagger-ui.html"
                    ).permitAll()
                    .pathMatchers("/api/v1/auth/**").permitAll()
                    .pathMatchers("/api/v1/**").authenticated()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt
                        .jwtDecoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthConverter)
                }
            }
            .build()
    }

    // 중앙화된 JWT 설정 사용
    @Bean
    fun jwtDecoder(jwtConfig: JwtConfig): ReactiveJwtDecoder {
        return org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
            .withSecretKey(
                SecretKeySpec(
                    jwtConfig.getSecretKeyBytes(),
                    JwtConfig.HMAC_ALGORITHM
                )
            )
            .build()
    }

    // roles 클레임 → ROLE_ 접두어 권한 매핑 (중앙화된 설정 사용)
    @Bean
    fun jwtAuthConverter(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        val delegate = JwtGrantedAuthoritiesConverter()
        delegate.setAuthoritiesClaimName(JwtConfig.ROLES_CLAIM) // 중앙화된 클레임 키 사용
        delegate.setAuthorityPrefix("ROLE_") // hasRole과 호환

        return ReactiveJwtAuthenticationConverterAdapter { jwt ->
            val authorities = delegate.convert(jwt)
            JwtAuthenticationToken(jwt, authorities, jwt.subject)
        }
    }
}