package com.techchallenge.oficina.autenticacao.dominio;

import com.techchallenge.oficina.shared.excecoes.NaoAutenticadoException;

/** Mesma mensagem para usuário inexistente, inativo ou senha errada, para não revelar qual deles falhou. */
public class CredenciaisInvalidasException extends NaoAutenticadoException {

	public CredenciaisInvalidasException() {
		super("Usuário ou senha inválidos");
	}
}
