package com.techchallenge.oficina.servicos.cadastrar;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CadastrarServicoRequest(
		@NotBlank String descricao,
		@NotNull BigDecimal valor) {

}
