package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.servicos.entidades.Servico;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record ServicoResponse(
		String descricao,
		BigDecimal valor
		) {
	
	
	public static ServicoResponse from(Servico servico) {
		return new ServicoResponse(
				servico.getDescricao(),
				servico.getValor());
	}

	public static List<ServicoResponse> from(List<Servico> servicos) {

		if(servicos == null){
			return Collections.emptyList();
		}

		return servicos.stream().map(ServicoResponse::from).collect(Collectors.toList());
	}
}
