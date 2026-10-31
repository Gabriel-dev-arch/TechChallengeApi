package com.techchallenge.oficina.ordem_servico.dominio.exception;

import com.techchallenge.oficina.shared.excecoes.ConflitoException;

/** A etapa pedida não pode ser executada no status atual da ordem de serviço. Vira HTTP 409. */
public class TransicaoInvalidaException extends ConflitoException {
	public TransicaoInvalidaException(String mensagem) {
		super(mensagem);
	}
}
