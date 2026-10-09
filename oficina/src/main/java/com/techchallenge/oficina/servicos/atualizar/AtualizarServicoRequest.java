package com.techchallenge.oficina.servicos.atualizar;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarServicoRequest(
		@NotBlank String descricao,
		@NotNull BigDecimal valor) {
}

