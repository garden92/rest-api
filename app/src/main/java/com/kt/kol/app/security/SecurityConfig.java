package com.kt.kol.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import com.kt.kol.common.config.JwtConfig;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
			ReactiveJwtDecoder jwtDecoder,
			Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthConverter) {
		return http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
				.authorizeExchange(ex -> ex
						.pathMatchers(HttpMethod.GET, "/actuator/health").permitAll()
						.pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**",
								"/api-docs/**",
								"/swagger-ui.html")
						.permitAll()
						.pathMatchers("/api/v1/auth/**").permitAll()
						.pathMatchers("/api/v1/**").authenticated()
						.anyExchange().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt
								.jwtDecoder(jwtDecoder)
								.jwtAuthenticationConverter(jwtAuthConverter)))
				.build();
	}

	// 중앙화된 JWT 설정 사용
	@Bean
	ReactiveJwtDecoder jwtDecoder(JwtConfig jwtConfig) {
		return org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
				.withSecretKey(new javax.crypto.spec.SecretKeySpec(
						jwtConfig.getSecretKeyBytes(), JwtConfig.HMAC_ALGORITHM))
				.build();
	}

	// roles 클레임 → ROLE_ 접두어 권한 매핑 (중앙화된 설정 사용)
	@Bean
	Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthConverter() {
		var delegate = new org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter();
		delegate.setAuthoritiesClaimName(JwtConfig.ROLES_CLAIM); // 중앙화된 클레임 키 사용
		delegate.setAuthorityPrefix("ROLE_"); // hasRole과 호환

		return new org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter(
				jwt -> {
					var authorities = delegate.convert(jwt);
					return new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(
							jwt, authorities, jwt.getSubject());
				});
	}
}
