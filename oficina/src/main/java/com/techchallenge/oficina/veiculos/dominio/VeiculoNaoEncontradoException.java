package com.techchallenge.oficina.veiculos.dominio;

import com.techchallenge.oficina.shared.excecoes.RecursoNaoEncontradoException;

import java.util.UUID;

public class VeiculoNaoEncontradoException extends RecursoNaoEncontradoException {

	public VeiculoNaoEncontradoException(UUID id) {
		super("Veículo não encontrado: " + id);
	}
}
