package com.techchallenge.oficina.ordem_servico.dominio.response;

import com.techchallenge.oficina.clientes.entidades.Cliente;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record ClienteResponse(
		String fullName,
		String email,
		String documento,
		String tipoDocumento,
		String telefone) {

	private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

	public static ClienteResponse from(Cliente cliente) {
		String tipo = cliente.getDocumento().length() == 11 ? "CPF" : "CNPJ";
		return new ClienteResponse(cliente.getFullName(), cliente.getEmail(), cliente.getDocumento(), tipo, cliente.getTelefone());
	}

	/** Converte o instante (UTC) para o horário de Brasília; null enquanto a entidade não foi persistida. */
	private static OffsetDateTime paraFusoLocal(Instant instante) {
		return instante == null ? null : instante.atZone(FUSO).toOffsetDateTime();
	}
}
