package com.techchallenge.oficina.ordem_servico.dominio.exception;

import java.util.UUID;

public class OrdemServicoNaoEncontradaException extends RuntimeException {

	public OrdemServicoNaoEncontradaException(UUID id) {
		super("Ordem de serviço não encontrado pelo id: " + id);
	}

	public OrdemServicoNaoEncontradaException(String placa) {
		super("Ordem de serviço não encontrado pela placa: " + placa);
	}
}
