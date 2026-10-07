package com.techchallenge.oficina.servicos.consultar;

import jakarta.validation.constraints.NotBlank;

public record ConsultarServicoRequest(
		@NotBlank String placa,
		@NotBlank String idServico
		) {

}
