package com.techchallenge.oficina.config;

import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Autenticação JWT (HS256, chave simétrica do {@code JWT_SECRET}) com perfis ADMIN e CLIENTE.
 * O token é emitido por {@code POST /autenticacao/login} e enviado no header {@code Authorization: Bearer <token>}.
 *
 * <ul>
 *   <li>401: sem token, token inválido ou expirado (não autenticado).</li>
 *   <li>403: token válido, mas o perfil não tem permissão para a rota (não autorizado).</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

	/** Rotas abertas: login, documentação da API e página de erro. */
	private static final String[] ROTAS_PUBLICAS = {
			"/autenticacao/login", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error"};

	private final SecretKey chave;

	SecurityConfig(@Value("${seguranca.jwt.secret}") String secret) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("JWT_SECRET deve ter pelo menos 32 caracteres (256 bits) para o HS256");
		}
		this.chave = new SecretKeySpec(bytes, "HmacSHA256");
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(a -> a
						.requestMatchers(ROTAS_PUBLICAS).permitAll()
						// Rotas liberadas ao CLIENTE (ex.: consulta da OS) devem ser declaradas aqui, antes do
						// anyRequest, com hasAnyRole("ADMIN", "CLIENTE"), e chamar ControleDeAcesso.verificarDono.
						.anyRequest().hasRole(Perfil.ADMIN.name()))
				.oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(conversorDePerfis())))
				.build();
	}

	@Bean
	JwtEncoder jwtEncoder() {
		return NimbusJwtEncoder.withSecretKey(chave).build();
	}

	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withSecretKey(chave).build();
	}

	/** Lê os perfis do claim "roles" do token como ROLE_ADMIN / ROLE_CLIENTE (usados em hasRole). */
	private static JwtAuthenticationConverter conversorDePerfis() {
		JwtGrantedAuthoritiesConverter perfis = new JwtGrantedAuthoritiesConverter();
		perfis.setAuthoritiesClaimName(Perfil.CLAIM_JWT);
		perfis.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
		conversor.setJwtGrantedAuthoritiesConverter(perfis);
		return conversor;
	}
}
