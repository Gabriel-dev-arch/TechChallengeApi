package com.techchallenge.oficina.autenticacao.dominio;

import com.techchallenge.oficina.autenticacao.entidades.Usuario;
import com.techchallenge.oficina.config.JwtConfig;
import com.techchallenge.oficina.config.JwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class GeradorDeToken {

	private final JwtEncoder encoder;
	private final Duration validade;

	public GeradorDeToken(JwtEncoder encoder, JwtProperties propriedades) {
		this.encoder = encoder;
		this.validade = Duration.ofMinutes(propriedades.expiracaoMinutos());
	}

	public Jwt gerar(Usuario usuario) {
		// iat/exp do JWT têm precisão de segundos; truncar aqui faz o expiraEm da resposta bater com o token
		Instant agora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(JwtConfig.EMISSOR)
				.subject(usuario.getUsername())
				.issuedAt(agora)
				.expiresAt(agora.plus(validade))
				.claim(JwtConfig.CLAIM_PERFIS, List.of(usuario.getPerfil().name()))
				.build();
		// Sem o header HS256 o Nimbus assume RS256 e falha com "Failed to select a JWK signing key".
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims));
	}
}
