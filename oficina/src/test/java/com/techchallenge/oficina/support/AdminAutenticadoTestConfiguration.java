package com.techchallenge.oficina.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Faz toda requisição do MockMvc ir com um JWT simulado de perfil ADMIN, para testar as APIs administrativas.
 * Uso: {@code @Import({PostgresTestConfiguration.class, AdminAutenticadoTestConfiguration.class})}.
 */
@TestConfiguration(proxyBeanMethods = false)
public class AdminAutenticadoTestConfiguration {

	@Bean
	MockMvcBuilderCustomizer requisicoesComoAdmin() {
		return builder -> builder.defaultRequest(get("/").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))));
	}
}
