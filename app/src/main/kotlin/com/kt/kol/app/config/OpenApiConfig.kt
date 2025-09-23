package com.kt.kol.app.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI(Swagger) 설정 클래스
 * JWT Bearer Token 인증을 포함한 API 문서화 설정
 */
@Configuration
class OpenApiConfig {

    companion object {
        private const val SECURITY_SCHEME_NAME = "bearerAuth"
    }

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("KOL REST API")
                    .description("KOS-O/L RestGW API Layer - Spring Boot WebFlux 기반 API")
                    .version("v1.0.0")
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("http://springdoc.org")
                    )
            )
            .addSecurityItem(
                SecurityRequirement()
                    .addList(SECURITY_SCHEME_NAME)
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        SECURITY_SCHEME_NAME, SecurityScheme()
                            .name(SECURITY_SCHEME_NAME)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT Bearer Token 인증")
                    )
            )
    }
}