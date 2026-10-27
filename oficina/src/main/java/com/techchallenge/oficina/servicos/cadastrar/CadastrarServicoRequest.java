package com.techchallenge.oficina.servicos.cadastrar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CadastrarServicoRequest(
		@NotBlank String descricao,
		@NotNull BigDecimal valor) {

}
