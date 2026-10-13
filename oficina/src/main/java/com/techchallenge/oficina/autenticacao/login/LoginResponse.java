package com.techchallenge.oficina.autenticacao.login;

/** Token de acesso no formato usado pelo OAuth2: enviar como {@code Authorization: Bearer <accessToken>}. */
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
