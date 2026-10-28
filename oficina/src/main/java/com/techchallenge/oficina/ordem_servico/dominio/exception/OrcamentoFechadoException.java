package com.techchallenge.oficina.ordem_servico.dominio.exception;

import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.shared.excecoes.ConflitoException;

import java.util.UUID;

/** Serviços e peças só podem ser incluídos enquanto o orçamento não foi enviado ao cliente. Vira HTTP 409. */
public class OrcamentoFechadoException extends ConflitoException {
	public OrcamentoFechadoException(UUID id, StatusOS status) {
		super("O orçamento da ordem de serviço " + id + " não pode mais ser alterado (status: "
				+ status.getDescricao() + ")");
	}
}
