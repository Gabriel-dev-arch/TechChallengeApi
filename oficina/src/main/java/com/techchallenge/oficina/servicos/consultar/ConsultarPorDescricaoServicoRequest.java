package com.techchallenge.oficina.servicos.consultar;

import jakarta.validation.constraints.NotBlank;

public record ConsultarPorDescricaoServicoRequest(
		@NotBlank String descricao
	) {
}
