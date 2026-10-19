package com.techchallenge.oficina.ordem_servico.dominio.exception;

import com.techchallenge.oficina.shared.excecoes.RecursoNaoEncontradoException;

import java.util.UUID;

public class OrdemServicoException extends RuntimeException {

	public OrdemServicoException(UUID id) {
		super("Ordem de serviço não encontrado pelo id: " + id);
	}

	public OrdemServicoException(String placa) {
		super("Ordem de serviço não encontrado pela: " + placa);
	}
}
