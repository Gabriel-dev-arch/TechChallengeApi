package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;

import java.util.List;
import java.util.UUID;

public record OrdemServicoResponse(
		UUID id,
        OrcamentoResponse orcamento,
        String diagnostico,
		String abertaEm,
		ClienteResponse cliente,
        VeiculoResponse veiculo,
        List<OrdemServicoItemResponse> itens,
        List<ServicoResponse> servicos) {

	public static OrdemServicoResponse from(OrdemServico ordemServico, StatusOS status) {

		var orcamento 			= OrcamentoResponse.from(ordemServico.getOrcamento(), status);
		var clienteResponse 	= ClienteResponse.from(ordemServico.getCliente());
		var veiculoResponse 	= VeiculoResponse.from(ordemServico.getVeiculo());
		var itensResponse 		= ordemServico.getItensQuantidade().stream()
				.map(OrdemServicoItemResponse::from)
				.toList();
		var servicosResponse 	= ServicoResponse.from(ordemServico.getServicos());

		return new OrdemServicoResponse(ordemServico.getId(), orcamento, ordemServico.getDiagnostico(), ordemServico.formatDate(), clienteResponse, veiculoResponse, itensResponse,servicosResponse);
	}

	public static OrdemServicoResponse from(OrdemServico ordemServico) {

		var orcamento 			= OrcamentoResponse.from(ordemServico.getOrcamento(), StatusOS.RECEBIDA);
		var clienteResponse 	= ClienteResponse.from(ordemServico.getCliente());
		var veiculoResponse 	= VeiculoResponse.from(ordemServico.getVeiculo());
		var itensResponse 		= ordemServico.getItensQuantidade().stream()
				.map(OrdemServicoItemResponse::from)
				.toList();
		var servicosResponse 	= ServicoResponse.from(ordemServico.getServicos());

		return new OrdemServicoResponse(ordemServico.getId(), orcamento, ordemServico.getDiagnostico(), ordemServico.formatDate(), clienteResponse, veiculoResponse, itensResponse,servicosResponse);
	}
}
