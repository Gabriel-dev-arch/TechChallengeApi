package com.techchallenge.oficina.ordem_servico.dominio.exception;

import com.techchallenge.oficina.shared.excecoes.RecursoNaoEncontradoException;

import java.util.UUID;

public class OrdemServicoException extends RecursoNaoEncontradoException {

	public OrdemServicoException(UUID id) {
		super("Cliente não encontrado: " + id);
	}

	public OrdemServicoException(String placa) {
		super("Ordem de serviço não encontrado pela: " + placa);
	}
}
