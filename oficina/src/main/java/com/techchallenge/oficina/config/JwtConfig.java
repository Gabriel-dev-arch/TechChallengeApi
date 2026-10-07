package com.techchallenge.oficina.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/** Emissão e validação de JWT HS256 com a mesma chave simétrica ({@code JWT_SECRET}). */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

	/** Valor da claim {@code iss}, exigido na validação. */
	public static final String EMISSOR = "oficina-api";

	/** Claim com os perfis do usuário (ex.: {@code ["ADMIN"]}), convertida em {@code ROLE_ADMIN}. */
	public static final String CLAIM_PERFIS = "roles";

	@Bean
	JwtEncoder jwtEncoder(JwtProperties propriedades) {
		return NimbusJwtEncoder.withSecretKey(propriedades.chave()).algorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	JwtDecoder jwtDecoder(JwtProperties propriedades) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(propriedades.chave())
				.macAlgorithm(MacAlgorithm.HS256).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(EMISSOR));
		return decoder;
	}
}
