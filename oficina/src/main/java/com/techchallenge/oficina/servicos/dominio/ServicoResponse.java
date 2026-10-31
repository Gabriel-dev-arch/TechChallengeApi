package com.techchallenge.oficina.servicos.dominio;

import com.techchallenge.oficina.servicos.entidades.Servico;

import java.math.BigDecimal;
import java.util.UUID;

public record ServicoResponse(
		UUID id,
		String descricao,
		BigDecimal valor
		) {
	
	
	public static ServicoResponse from(Servico servico) {
		return new ServicoResponse(
				servico.getId(),
				servico.getDescricao(),
				servico.getValor());
	}
}
