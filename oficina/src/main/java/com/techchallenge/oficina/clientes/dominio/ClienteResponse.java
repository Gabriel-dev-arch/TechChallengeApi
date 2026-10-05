package com.techchallenge.oficina.clientes.dominio;

import com.techchallenge.oficina.clientes.entidades.Cliente;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

public record ClienteResponse(
		UUID id,
		String firstName,
		String lastName,
		String fullName,
		String email,
		String documento,
		String tipoDocumento,
		String telefone,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

	public static ClienteResponse from(Cliente cliente) {
		String tipo = cliente.getDocumento().length() == 11 ? "CPF" : "CNPJ";
		return new ClienteResponse(cliente.getId(), cliente.getFirstName(), cliente.getLastName(),
				cliente.getFullName(), cliente.getEmail(), cliente.getDocumento(), tipo, cliente.getTelefone(),
				paraFusoLocal(cliente.getCreatedAt()), paraFusoLocal(cliente.getUpdatedAt()));
	}

	/** Converte o instante (UTC) para o horário de Brasília; null enquanto a entidade não foi persistida. */
	private static OffsetDateTime paraFusoLocal(Instant instante) {
		return instante == null ? null : instante.atZone(FUSO).toOffsetDateTime();
	}
}
