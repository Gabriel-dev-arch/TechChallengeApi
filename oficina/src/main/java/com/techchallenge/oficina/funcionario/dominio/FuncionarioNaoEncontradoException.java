package com.techchallenge.oficina.funcionario.dominio;

import com.techchallenge.oficina.shared.excecoes.RecursoNaoEncontradoException;

import java.util.UUID;

public class FuncionarioNaoEncontradoException extends RecursoNaoEncontradoException {

	public FuncionarioNaoEncontradoException(UUID id) {
		super("Funcionario não encontrado: " + id);
	}
}
