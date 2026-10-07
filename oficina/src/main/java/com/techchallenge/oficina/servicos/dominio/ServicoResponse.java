package com.techchallenge.oficina.servicos.dominio;

import java.math.BigDecimal;
import java.util.UUID;

import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.servicos.entidades.StatusServico;

public record ServicoResponse(
		UUID id,
		String placa,
		StatusServico status,
		String diagnostico,
		BigDecimal orcamentoTotal
		) {
	
	
	public static ServicoResponse from(Servico servico) {
		return new ServicoResponse(
				servico.getId(), 
				servico.getVeiculo().getPlaca(), 
				servico.getStatus(),
				servico.getDiagnostico(), 
				servico.getOrcamentoTotal());
	}

}
