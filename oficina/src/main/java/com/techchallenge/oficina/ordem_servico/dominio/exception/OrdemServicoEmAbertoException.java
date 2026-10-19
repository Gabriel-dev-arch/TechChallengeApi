package com.techchallenge.oficina.ordem_servico.dominio.exception;

import com.techchallenge.oficina.shared.excecoes.RecursoNaoEncontradoException;

import java.util.UUID;

public class OrdemServicoEmAbertoException extends RuntimeException {

	public OrdemServicoEmAbertoException(UUID id) {
		super("Veículo com ordem de serviço em aberto: " + id);
	}

	public OrdemServicoEmAbertoException(String placa) {
		super("Veículo com ordem de serviço em aberto: " + placa);
	}
}
