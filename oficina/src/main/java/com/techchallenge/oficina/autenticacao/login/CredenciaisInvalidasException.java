package com.techchallenge.oficina.autenticacao.login;

public class CredenciaisInvalidasException extends RuntimeException {

	public CredenciaisInvalidasException() {
		super("Usuário ou senha inválidos");
	}
}
