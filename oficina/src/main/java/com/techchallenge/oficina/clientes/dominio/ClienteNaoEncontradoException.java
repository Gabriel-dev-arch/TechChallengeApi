package com.techchallenge.oficina.clientes.dominio;

import java.util.UUID;

public class ClienteNaoEncontradoException extends RuntimeException {

	public ClienteNaoEncontradoException(UUID id) {
		super("Cliente não encontrado: " + id);
	}
}
