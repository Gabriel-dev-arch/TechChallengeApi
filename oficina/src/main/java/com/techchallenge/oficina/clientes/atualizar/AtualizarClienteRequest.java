package com.techchallenge.oficina.clientes.atualizar;

import com.techchallenge.oficina.clientes.dominio.CpfCnpj;
import com.techchallenge.oficina.clientes.dominio.Senha;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarClienteRequest(
		@NotBlank @Size(max = 75) String firstName,
		@NotBlank @Size(max = 75) String lastName,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @CpfCnpj String documento,
		@Size(max = 20) String telefone,
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) @Senha String senha) {
}
