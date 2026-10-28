package com.techchallenge.oficina.ordem_servico.cadastrar.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CadastrarOSRequest(
		List<UUID> servicos,
		List<ItemQuantidade> itens,
		String diagnostico,
		@NotBlank String placa,
		@NotNull @Valid FuncionarioRequest funcionario
		) {
}
