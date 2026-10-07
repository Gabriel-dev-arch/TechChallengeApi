package com.techchallenge.oficina.veiculos.dominio;

import com.techchallenge.oficina.veiculos.entidades.Veiculo;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public record VeiculoResponse(
		UUID id,
		String placa,
		String marca,
		String modelo,
		Integer ano,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

	public static VeiculoResponse from(Veiculo veiculo) {
		return new VeiculoResponse(veiculo.getId(), veiculo.getPlaca(), veiculo.getMarca(), veiculo.getModelo(),
				veiculo.getAno(), paraFusoLocal(veiculo.getCreatedAt()), paraFusoLocal(veiculo.getUpdatedAt()));
	}

	private static OffsetDateTime paraFusoLocal(Instant instante) {
		return instante == null ? null : instante.atZone(FUSO).toOffsetDateTime();
	}
}
