package com.techchallenge.oficina.config;

import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Todas as rotas exigem JWT com perfil ADMIN, exceto o login, o Swagger e {@code /publico/**}
 * (consultas que o cliente final faz sem login, como o andamento da OS).
 */
@Configuration
public class SecurityConfig {

	private static final String[] ROTAS_SWAGGER = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"};

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) throws Exception {
		// 401 e 403 são repassados ao GlobalExceptionHandler, para saírem em problem+json como os outros erros
		AuthenticationEntryPoint bearer = new BearerTokenAuthenticationEntryPoint();
		AuthenticationEntryPoint naoAutenticado = (request, response, ex) -> {
			bearer.commence(request, response, ex); // header WWW-Authenticate: Bearer (RFC 6750)
			resolver.resolveException(request, response, null, ex);
		};
		AccessDeniedHandler acessoNegado = (request, response, ex) -> resolver.resolveException(request, response, null, ex);

		return http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(a -> a
						// o dispatch interno de erro já passou pela autorização da requisição original;
						// sem isso, um 500 numa rota pública viraria 401
						.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
						.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
						.requestMatchers(ROTAS_SWAGGER).permitAll()
						.requestMatchers("/publico/**").permitAll()
						.anyRequest().hasRole(Perfil.ADMIN.name()))
				.oauth2ResourceServer(o -> o
						.jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDePerfis()))
						.authenticationEntryPoint(naoAutenticado)
						.accessDeniedHandler(acessoNegado))
				.exceptionHandling(e -> e
						.authenticationEntryPoint(naoAutenticado)
						.accessDeniedHandler(acessoNegado))
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/** Converte a claim {@code roles} do token (ex.: {@code ["ADMIN"]}) nas authorities {@code ROLE_ADMIN}. */
	private JwtAuthenticationConverter conversorDePerfis() {
		JwtGrantedAuthoritiesConverter perfis = new JwtGrantedAuthoritiesConverter();
		perfis.setAuthoritiesClaimName(JwtConfig.CLAIM_PERFIS);
		perfis.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
		conversor.setJwtGrantedAuthoritiesConverter(perfis);
		return conversor;
	}
}
