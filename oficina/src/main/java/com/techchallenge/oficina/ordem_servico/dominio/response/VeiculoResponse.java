package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.veiculos.entidades.Veiculo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record VeiculoResponse(
		String placa,
		String marca,
		String modelo,
		Integer ano) {

	private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

	public static VeiculoResponse from(Veiculo veiculo) {
		return new VeiculoResponse(veiculo.getPlaca(), veiculo.getMarca(), veiculo.getModelo(),
				veiculo.getAno());
	}

	private static OffsetDateTime paraFusoLocal(Instant instante) {
		return instante == null ? null : instante.atZone(FUSO).toOffsetDateTime();
	}
}
