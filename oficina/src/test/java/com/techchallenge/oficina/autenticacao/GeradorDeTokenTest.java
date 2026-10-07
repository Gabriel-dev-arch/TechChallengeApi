package com.techchallenge.oficina.autenticacao;

import com.techchallenge.oficina.autenticacao.dominio.GeradorDeToken;
import com.techchallenge.oficina.autenticacao.dominio.Perfil;
import com.techchallenge.oficina.autenticacao.entidades.Usuario;
import com.techchallenge.oficina.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static java.time.temporal.ChronoUnit.SECONDS;

class GeradorDeTokenTest {

	private static final JwtProperties PROPRIEDADES =
			new JwtProperties("chave-de-teste-do-gerador-com-mais-de-32-caracteres", 30);

	private final GeradorDeToken gerador =
			new GeradorDeToken(NimbusJwtEncoder.withSecretKey(PROPRIEDADES.chave()).build(), PROPRIEDADES);

	private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(PROPRIEDADES.chave()).build();

	@Test
	void geraTokenHs256ComClaimsDoUsuario() {
		Jwt token = decoder.decode(gerador.gerar(new Usuario("admin", "hash", Perfil.ADMIN)).getTokenValue());

		assertThat(token.getHeaders()).containsEntry("alg", "HS256");
		assertThat(token.getSubject()).isEqualTo("admin");
		assertThat(token.getClaimAsStringList("roles")).containsExactly("ADMIN");
		assertThat(token.getClaimAsString("iss")).isEqualTo("oficina-api");
		assertThat(token.getIssuedAt()).isNotNull();
	}

	@Test
	void expiracaoRespeitaAConfiguracao() {
		Instant antes = Instant.now();

		Jwt emitido = gerador.gerar(new Usuario("admin", "hash", Perfil.ADMIN));
		Jwt token = decoder.decode(emitido.getTokenValue());

		assertThat(Duration.between(token.getIssuedAt(), token.getExpiresAt())).isEqualTo(Duration.ofMinutes(30));
		assertThat(token.getExpiresAt()).isCloseTo(antes.plus(Duration.ofMinutes(30)), within(5, SECONDS));
		// o expiraEm devolvido no login sai deste Jwt e precisa ser o mesmo instante gravado no token
		assertThat(emitido.getExpiresAt()).isEqualTo(token.getExpiresAt());
	}
}
