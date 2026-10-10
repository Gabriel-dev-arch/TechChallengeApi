package com.techchallenge.oficina.clientes.dominio;

import com.techchallenge.oficina.shared.excecoes.RecursoNaoEncontradoException;

import java.util.UUID;

public class ClienteNaoEncontradoException extends RecursoNaoEncontradoException {

	public ClienteNaoEncontradoException(UUID id) {
		super("Cliente não encontrado: " + id);
	}
}
