package com.techchallenge.oficina.servicos.atualizar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AtualizarServicoRequest(
		@NotBlank String descricao,
		@NotNull BigDecimal valor) {
}

