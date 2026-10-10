package com.techchallenge.oficina.servicos.remover;

import jakarta.validation.constraints.NotBlank;

public record RemoverServicoRequest(
		@NotBlank String idServico
		) {
}
