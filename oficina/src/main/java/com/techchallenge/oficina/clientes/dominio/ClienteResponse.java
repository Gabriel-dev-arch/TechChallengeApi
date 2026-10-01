package com.techchallenge.oficina.clientes.dominio;

import com.techchallenge.oficina.clientes.entidades.Cliente;

import java.time.Instant;
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
		Instant createdAt,
		Instant updatedAt) {

	public static ClienteResponse from(Cliente cliente) {
		String tipo = cliente.getDocumento().length() == 11 ? "CPF" : "CNPJ";
		return new ClienteResponse(cliente.getId(), cliente.getFirstName(), cliente.getLastName(),
				cliente.getFullName(), cliente.getEmail(), cliente.getDocumento(), tipo, cliente.getTelefone(),
				cliente.getCreatedAt(), cliente.getUpdatedAt());
	}
}
