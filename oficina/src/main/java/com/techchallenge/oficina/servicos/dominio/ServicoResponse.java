package com.techchallenge.oficina.servicos.dominio;

import java.math.BigDecimal;
import java.util.UUID;

import com.techchallenge.oficina.servicos.entidades.Servico;

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
