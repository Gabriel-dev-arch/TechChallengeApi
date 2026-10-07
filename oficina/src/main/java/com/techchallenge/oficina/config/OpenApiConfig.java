package com.techchallenge.oficina.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita o botão Authorize do Swagger e marca todas as operações como protegidas por JWT.
 * Rotas públicas tiram o cadeado com {@code @SecurityRequirements} vazio (ver LoginController).
 */
@Configuration
public class OpenApiConfig {

	private static final String ESQUEMA_JWT = "bearerAuth";

	@Bean
	OpenAPI openApi() {
		SecurityScheme bearer = new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT")
				.description("Faça login em POST /auth/login e cole aqui o accessToken (sem o prefixo \"Bearer\").");
		return new OpenAPI()
				.components(new Components().addSecuritySchemes(ESQUEMA_JWT, bearer))
				.addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
	}
}
