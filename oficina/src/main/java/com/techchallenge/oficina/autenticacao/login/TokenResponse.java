package com.techchallenge.oficina.autenticacao.login;

import org.springframework.security.oauth2.jwt.Jwt;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record TokenResponse(
		String accessToken,
		String tipo,
		OffsetDateTime expiraEm) {

	private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

	public static TokenResponse from(Jwt token) {
		return new TokenResponse(token.getTokenValue(), "Bearer",
				token.getExpiresAt().atZone(FUSO).toOffsetDateTime());
	}
}
