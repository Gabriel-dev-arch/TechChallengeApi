package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.ordem_servico.dominio.CalculoOrcamento;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;

import java.math.BigDecimal;
import java.util.List;

public record OrdemServicoResponse(
		 OrcamentoResponse orcamento,
		 String diagnostico,
		 ClienteResponse cliente,
		 VeiculoResponse veiculo,
		 List<PecaInsumoResponse> itens,
		 List<ServicoResponse> servicos) {

	public static OrdemServicoResponse from(OrdemServico ordemServico) {

		String diagnostico = ordemServico.getDiagnostico();
		OrcamentoResponse orcamento = OrcamentoResponse.from(
				CalculoOrcamento.calculaOrcamento(ordemServico.getItens(), ordemServico.getServicos()),
				ordemServico.getStatus());

		var clienteResponse = ClienteResponse.from(ordemServico.getCliente());
		var veiculoResponse = VeiculoResponse.from(ordemServico.getVeiculo());
		var pecaInsumoResponse = PecaInsumoResponse.from(ordemServico.getItens());
		var servicosResponse = ServicoResponse.from(ordemServico.getServicos());

		return new OrdemServicoResponse(orcamento, diagnostico, clienteResponse, veiculoResponse, pecaInsumoResponse,servicosResponse);
	}
}
