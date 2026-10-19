package com.techchallenge.oficina.ordem_servico.dominio;

import com.techchallenge.oficina.clientes.entidades.Cliente;
import com.techchallenge.oficina.ordem_servico.entidades.OrdemServico;
import com.techchallenge.oficina.ordem_servico.entidades.StatusOS;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import com.techchallenge.oficina.servicos.entidades.Servico;
import com.techchallenge.oficina.veiculos.entidades.Veiculo;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public record OrdemServicoResponse(

		 BigDecimal orcamento,
		 ClienteResponse clienteResponse,
		 VeiculoResponse veiculoResponse,
		 List<PecaInsumoResponse> pecaInsumoResponse,
		 List<ServicoResponse> servicosResponse) {

	public static OrdemServicoResponse from(OrdemServico ordemServico) {

		BigDecimal valorOrcamento = Orcamento.from(ordemServico.getItens(), ordemServico.getServicos());
		var clienteResponse = ClienteResponse.from(ordemServico.getCliente());
		var veiculoResponse = VeiculoResponse.from(ordemServico.getVeiculo());
		var pecaInsumoResponse = PecaInsumoResponse.from(ordemServico.getItens());
		var servicosResponse = ServicoResponse.from(ordemServico.getServicos());

		return new OrdemServicoResponse(valorOrcamento, clienteResponse, veiculoResponse, pecaInsumoResponse,servicosResponse);
	}
}
