package com.techchallenge.oficina.ordem_servico.dominio.exception;

import com.techchallenge.oficina.shared.excecoes.RecursoNaoEncontradoException;

import java.util.UUID;

public class VeiculoNaoEncontradoException extends RuntimeException {

	public VeiculoNaoEncontradoException(UUID id) {
		super("Veículo não encontrado: " + id);
	}

	public VeiculoNaoEncontradoException(String placa) {
		super("Veículo não encontrado: " + placa);
	}
}
