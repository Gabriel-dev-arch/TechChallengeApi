package com.techchallenge.oficina.autenticacao.login;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
