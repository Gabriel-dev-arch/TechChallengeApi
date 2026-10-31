package com.techchallenge.oficina.servicos.dominio;

import java.util.UUID;

public class ServicoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

		
	public ServicoNaoEncontradoException(UUID id) {
		super("Servico não encontrado: " + id);
	}
}

