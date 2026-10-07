package com.techchallenge.oficina.veiculos.dominio;

import java.util.UUID;

public class VeiculoNaoEncontradoException extends RuntimeException {

	public VeiculoNaoEncontradoException(UUID id) {
		super("Veículo não encontrado: " + id);
	}
}
