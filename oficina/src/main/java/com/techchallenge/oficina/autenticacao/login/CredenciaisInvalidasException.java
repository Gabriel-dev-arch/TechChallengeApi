package com.techchallenge.oficina.autenticacao.login;

/** Usuário ou senha incorretos no login. Vira HTTP 401. */
public class CredenciaisInvalidasException extends RuntimeException {

	public CredenciaisInvalidasException() {
		super("Usuário ou senha inválidos");
	}
}
